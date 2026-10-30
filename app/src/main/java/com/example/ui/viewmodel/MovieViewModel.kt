package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.FamilyMember
import com.example.data.local.Movie
import com.example.data.local.SessionManager
import com.example.data.model.AppUpdateInfo
import com.example.data.model.DeletedMemberInfo
import com.example.data.model.MovieNight
import com.example.data.model.MovieNotification
import com.example.data.model.MovieSearchResult
import com.example.data.model.Review
import com.example.data.remote.DownloadState
import com.example.data.remote.UpdateDownloader
import com.example.data.repository.MovieRepository
import java.io.File
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

enum class DurationFilter(val label: String, val maxMinutes: Int?, val minMinutes: Int?) {
    ALL("Toutes durées", null, null),
    SHORT("< 1h30", 90, null),
    MEDIUM("1h30 - 2h", 120, 90),
    LONG("> 2h", null, 120)
}

enum class WatchStatusFilter(val label: String) {
    TO_WATCH("À voir"),
    WATCHED("Déjà vus"),
    ALL("Tous les films")
}

enum class RandomDurationFilter(val label: String) {
    ALL("Toutes"),
    UNDER_2H("Moins de 2h"),
    OVER_2H("+ de 2h")
}

data class FilterState(
    val searchQuery: String = "",
    val memberId: String? = null,
    val genre: String? = null,
    val duration: DurationFilter = DurationFilter.ALL,
    val status: WatchStatusFilter = WatchStatusFilter.TO_WATCH,
    val audienceType: String? = null, // "FAMILY", "SOLO", "GROUP"
    val streamingService: String? = null, // "Netflix", "Amazon Prime Video", "Gratuit"
    val showOnlyPrivateNiche: Boolean = false
)

class MovieViewModel(
    private val repository: MovieRepository,
    private val sessionManager: SessionManager,
    private val updateDownloader: UpdateDownloader? = null
) : ViewModel() {

    val currentVersionCode: Int = BuildConfig.VERSION_CODE
    val currentVersionName: String = BuildConfig.VERSION_NAME

    val appUpdateInfo: StateFlow<AppUpdateInfo?> = repository.appUpdateInfo
    val movieNotifications: StateFlow<List<MovieNotification>> = repository.movieNotifications
    val movieNights: StateFlow<List<MovieNight>> = repository.movieNights
    val cloudSyncStatus: StateFlow<String> = repository.cloudSyncStatus

    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.Idle)
    val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    private val _isUpdateDialogDismissed = MutableStateFlow(false)
    val isUpdateDialogDismissed: StateFlow<Boolean> = _isUpdateDialogDismissed.asStateFlow()

    val allMovies: StateFlow<List<Movie>> = repository.allMovies
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val allMembers: StateFlow<List<FamilyMember>> = repository.allMembers
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val deletedMembers: StateFlow<List<DeletedMemberInfo>> = repository.deletedMembers

    val recoverableMembers: StateFlow<List<DeletedMemberInfo>> = combine(
        allMembers,
        repository.deletedMembers,
        allMovies
    ) { currentMembers, deletedList, movies ->
        val currentIds = currentMembers.map { it.id }.toSet()
        val result = mutableListOf<DeletedMemberInfo>()

        // 1. Members from repository deleted list (only non-expired, within 7 days)
        for (delInfo in deletedList) {
            if (!delInfo.isExpired && !currentIds.contains(delInfo.member.id) && result.none { it.member.id == delInfo.member.id }) {
                result.add(delInfo)
            }
        }

        // 2. Default members that are missing
        for (defaultMember in com.example.data.local.DEFAULT_FAMILY_MEMBERS) {
            if (!currentIds.contains(defaultMember.id) && result.none { it.member.id == defaultMember.id }) {
                result.add(DeletedMemberInfo(member = defaultMember, deletedAt = System.currentTimeMillis()))
            }
        }

        // 3. Authors found in movies or reviews
        for (movie in movies) {
            val authorId = movie.addedByMemberId
            if (authorId.isNotBlank() && authorId != "all" && !currentIds.contains(authorId) && result.none { it.member.id == authorId }) {
                result.add(
                    DeletedMemberInfo(
                        member = FamilyMember(
                            id = authorId,
                            name = movie.addedByMemberName.ifBlank { "Membre" },
                            avatarEmoji = movie.addedByMemberEmoji.ifBlank { "🎬" },
                            colorHex = 0xFF448AFF,
                            role = "Membre de la famille",
                            pinCode = "1234",
                            preferredGenres = "Tous genres",
                            isAdmin = false
                        ),
                        deletedAt = System.currentTimeMillis()
                    )
                )
            }
        }

        result
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Current Active Member
    private val _activeMember = MutableStateFlow<FamilyMember?>(null)
    val activeMember: StateFlow<FamilyMember?> = _activeMember.asStateFlow()

    // Admin Impersonation: holds the original admin session when Steph acts as another member
    private val _originalAdminSession = MutableStateFlow<FamilyMember?>(null)
    val originalAdminSession: StateFlow<FamilyMember?> = _originalAdminSession.asStateFlow()

    // PIN Authentication Dialog State
    private val _pendingMemberForAuth = MutableStateFlow<FamilyMember?>(null)
    val pendingMemberForAuth: StateFlow<FamilyMember?> = _pendingMemberForAuth.asStateFlow()

    private val _pinAuthError = MutableStateFlow<String?>(null)
    val pinAuthError: StateFlow<String?> = _pinAuthError.asStateFlow()

    // Filter State
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    // Filtered movies respecting privacy & active member view
    val filteredMovies: StateFlow<List<Movie>> = combine(allMovies, _filterState, _activeMember) { movies, filter, currentMember ->
        movies.filter { movie ->
            // Private niche movie check: strictly personal, only visible to the author
            if (movie.isPrivate && (currentMember == null || movie.addedByMemberId != currentMember.id)) {
                return@filter false
            }

            // If user explicitly asks for private niche list:
            if (filter.showOnlyPrivateNiche) {
                if (!movie.isPrivate) return@filter false
            } else {
                // In main shared catalog, don't mix personal niche movies unless member filter is on
                if (movie.isPrivate && filter.memberId == null) {
                    return@filter false
                }
            }

            // Privacy check: Solo movies of other members are hidden from common list unless admin
            val isVisibleToCurrentMember = when (movie.targetAudienceType) {
                "FAMILY" -> true
                "SOLO" -> currentMember == null || currentMember.isAdmin || movie.addedByMemberId == currentMember.id
                "GROUP" -> true // Visible so members can view interested viewers and join/leave
                else -> true
            }
            if (!isVisibleToCurrentMember) return@filter false

            // Search query
            val matchesQuery = filter.searchQuery.isBlank() ||
                    movie.title.contains(filter.searchQuery, ignoreCase = true) ||
                    movie.director.contains(filter.searchQuery, ignoreCase = true) ||
                    movie.genres.contains(filter.searchQuery, ignoreCase = true)

            // Member filter
            val matchesMember = filter.memberId == null || movie.addedByMemberId == filter.memberId

            // Genre filter
            val matchesGenre = filter.genre == null || movie.genres.contains(filter.genre, ignoreCase = true)

            // Duration filter
            val matchesDuration = when (filter.duration) {
                DurationFilter.ALL -> true
                DurationFilter.SHORT -> movie.durationMinutes <= 90
                DurationFilter.MEDIUM -> movie.durationMinutes in 91..120
                DurationFilter.LONG -> movie.durationMinutes > 120
            }

            // Watch status filter
            val matchesStatus = when (filter.status) {
                WatchStatusFilter.TO_WATCH -> !movie.isWatched
                WatchStatusFilter.WATCHED -> movie.isWatched
                WatchStatusFilter.ALL -> true
            }

            // Audience type filter
            val matchesAudience = filter.audienceType == null || movie.targetAudienceType == filter.audienceType

            // Streaming service filter
            val matchesStreaming = filter.streamingService == null || movie.streamingServices.contains(filter.streamingService)

            matchesQuery && matchesMember && matchesGenre && matchesDuration && matchesStatus && matchesAudience && matchesStreaming
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Random Selector State with Multiple Viewers Selection (C+A+S, C+S, A+C, etc.)
    private val _selectedViewerIds = MutableStateFlow<Set<String>>(emptySet())
    val selectedViewerIds: StateFlow<Set<String>> = _selectedViewerIds.asStateFlow()

    private val _randomMemberFilterId = MutableStateFlow<String?>(null)
    val randomMemberFilterId: StateFlow<String?> = _randomMemberFilterId.asStateFlow()

    private val _randomGenreFilter = MutableStateFlow<String?>(null)
    val randomGenreFilter: StateFlow<String?> = _randomGenreFilter.asStateFlow()

    private val _randomDurationFilter = MutableStateFlow(RandomDurationFilter.ALL)
    val randomDurationFilter: StateFlow<RandomDurationFilter> = _randomDurationFilter.asStateFlow()

    private val _randomMaxDurationFilter = MutableStateFlow<Int?>(null)
    val randomMaxDurationFilter: StateFlow<Int?> = _randomMaxDurationFilter.asStateFlow()

    private val _randomStreamingFilter = MutableStateFlow<String?>(null)
    val randomStreamingFilter: StateFlow<String?> = _randomStreamingFilter.asStateFlow()

    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private val _spinningDisplayMovie = MutableStateFlow<Movie?>(null)
    val spinningDisplayMovie: StateFlow<Movie?> = _spinningDisplayMovie.asStateFlow()

    private val _winningMovie = MutableStateFlow<Movie?>(null)
    val winningMovie: StateFlow<Movie?> = _winningMovie.asStateFlow()

    private val _spinErrorMessage = MutableStateFlow<String?>(null)
    val spinErrorMessage: StateFlow<String?> = _spinErrorMessage.asStateFlow()

    // Online Search & Add State
    private val _onlineQuery = MutableStateFlow("")
    val onlineQuery: StateFlow<String> = _onlineQuery.asStateFlow()

    private val _isOnlineSearching = MutableStateFlow(false)
    val isOnlineSearching: StateFlow<Boolean> = _isOnlineSearching.asStateFlow()

    private val _onlineSearchResults = MutableStateFlow<List<MovieSearchResult>>(emptyList())
    val onlineSearchResults: StateFlow<List<MovieSearchResult>> = _onlineSearchResults.asStateFlow()

    private val _selectedOnlineMovie = MutableStateFlow<MovieSearchResult?>(null)
    val selectedOnlineMovie: StateFlow<MovieSearchResult?> = _selectedOnlineMovie.asStateFlow()

    // Detail Dialog State
    private val _movieForDetail = MutableStateFlow<Movie?>(null)
    val movieForDetail: StateFlow<Movie?> = _movieForDetail.asStateFlow()

    // Feedback
    private val _userFeedback = MutableStateFlow<String?>(null)
    val userFeedback: StateFlow<String?> = _userFeedback.asStateFlow()

    private var searchJob: Job? = null
    private var spinJob: Job? = null

    init {
        viewModelScope.launch {
            allMembers.collect { members ->
                if (members.isNotEmpty()) {
                    if (_activeMember.value == null) {
                        val savedId = sessionManager.getLoggedInMemberId()
                        val savedMember = members.find { it.id == savedId }
                        if (savedMember != null) {
                            _activeMember.value = savedMember
                        }
                    }
                    if (_selectedViewerIds.value.isEmpty()) {
                        _selectedViewerIds.value = members.map { it.id }.toSet()
                    }
                }
            }
        }

        viewModelScope.launch {
            allMovies.collect { movies ->
                val current = _movieForDetail.value
                if (current != null) {
                    val updated = movies.find { it.id == current.id }
                    if (updated != null) {
                        _movieForDetail.value = updated
                    }
                }
            }
        }
    }

    fun login(member: FamilyMember) {
        sessionManager.saveLoggedInMemberId(member.id)
        _originalAdminSession.value = null
        _activeMember.value = member
        _userFeedback.value = "Connecté en tant que ${member.name} !"
    }

    fun logout() {
        sessionManager.clearSession()
        _originalAdminSession.value = null
        _activeMember.value = null
        _userFeedback.value = "Déconnexion réussie."
    }

    fun impersonateMember(targetMember: FamilyMember) {
        val current = _activeMember.value
        // If not already impersonating and currently admin, record admin session
        if (_originalAdminSession.value == null && current?.isAdmin == true) {
            _originalAdminSession.value = current
        }
        _activeMember.value = targetMember
        _userFeedback.value = "Mode Admin : vous agissez maintenant en tant que ${targetMember.name}"
    }

    fun exitImpersonation() {
        val admin = _originalAdminSession.value ?: return
        _activeMember.value = admin
        _originalAdminSession.value = null
        _userFeedback.value = "Retour au profil administrateur (${admin.name})."
    }

    // Security & Profile Authentication
    fun requestSelectMember(member: FamilyMember) {
        if (_activeMember.value?.id == member.id) return
        _pendingMemberForAuth.value = member
        _pinAuthError.value = null
    }

    fun submitPinAuth(pin: String) {
        val pending = _pendingMemberForAuth.value ?: return
        if (pin.trim() == pending.pinCode || pending.pinCode.isEmpty()) {
            login(pending)
            _pendingMemberForAuth.value = null
            _pinAuthError.value = null
        } else {
            _pinAuthError.value = "Code PIN incorrect (Code test: ${pending.pinCode})"
        }
    }

    fun cancelPinAuth() {
        _pendingMemberForAuth.value = null
        _pinAuthError.value = null
    }

    fun updateMemberPreferredGenres(genres: String) {
        val member = _activeMember.value ?: return
        val updated = member.copy(preferredGenres = genres)
        _activeMember.value = updated
        viewModelScope.launch {
            repository.insertMember(updated)
            _userFeedback.value = "Préférences de genres mises à jour !"
        }
    }

    // Filter mutations
    fun setSearchQuery(query: String) {
        _filterState.value = _filterState.value.copy(searchQuery = query)
    }

    fun setMemberFilter(memberId: String?) {
        _filterState.value = _filterState.value.copy(memberId = memberId)
    }

    fun setGenreFilter(genre: String?) {
        _filterState.value = _filterState.value.copy(genre = genre)
    }

    fun setDurationFilter(duration: DurationFilter) {
        _filterState.value = _filterState.value.copy(duration = duration)
    }

    fun setWatchStatusFilter(status: WatchStatusFilter) {
        _filterState.value = _filterState.value.copy(status = status)
    }

    fun setAudienceFilter(audience: String?) {
        _filterState.value = _filterState.value.copy(audienceType = audience)
    }

    fun setStreamingFilter(streaming: String?) {
        _filterState.value = _filterState.value.copy(streamingService = streaming)
    }

    fun resetFilters() {
        _filterState.value = FilterState()
    }

    // Random Selector: Group configuration (C+A+S, C+S, A+C, etc.)
    fun toggleViewer(memberId: String) {
        val current = _selectedViewerIds.value.toMutableSet()
        if (current.contains(memberId)) {
            if (current.size > 1) { // keep at least 1 viewer
                current.remove(memberId)
            }
        } else {
            current.add(memberId)
        }
        _selectedViewerIds.value = current
        _winningMovie.value = null
        _spinErrorMessage.value = null
    }

    fun selectAllViewers() {
        _selectedViewerIds.value = allMembers.value.map { it.id }.toSet()
        _winningMovie.value = null
        _spinErrorMessage.value = null
    }

    fun setRandomMemberFilter(memberId: String?) {
        _randomMemberFilterId.value = memberId
        _winningMovie.value = null
        _spinErrorMessage.value = null
    }

    fun setRandomGenreFilter(genre: String?) {
        _randomGenreFilter.value = genre
        _winningMovie.value = null
        _spinErrorMessage.value = null
    }

    fun setRandomDurationFilter(filter: RandomDurationFilter) {
        _randomDurationFilter.value = filter
        _winningMovie.value = null
        _spinErrorMessage.value = null
    }

    fun setRandomMaxDurationFilter(maxMinutes: Int?) {
        _randomMaxDurationFilter.value = maxMinutes
        _winningMovie.value = null
        _spinErrorMessage.value = null
    }

    fun setRandomStreamingFilter(streaming: String?) {
        _randomStreamingFilter.value = streaming
        _winningMovie.value = null
        _spinErrorMessage.value = null
    }

    fun getEligibleRandomMovies(): List<Movie> {
        val viewers = _selectedViewerIds.value
        val memberFilter = _randomMemberFilterId.value
        val genreFilter = _randomGenreFilter.value
        val durationFilter = _randomDurationFilter.value
        val maxDuration = _randomMaxDurationFilter.value
        val streaming = _randomStreamingFilter.value
        val allMemberIds = allMembers.value.map { it.id }.toSet()
        val isAllFamily = viewers.size == allMemberIds.size

        return allMovies.value.filter { movie ->
            // Exclude private niche movies from family roulette
            if (movie.isPrivate) return@filter false

            // Unwatched first
            if (movie.isWatched) return@filter false

            // Member who added it filter
            if (memberFilter != null && movie.addedByMemberId != memberFilter) return@filter false

            // Genre filter
            if (genreFilter != null && !movie.genres.contains(genreFilter, ignoreCase = true)) {
                return@filter false
            }

            // Duration filter (Moins de 2h vs + de 2h)
            when (durationFilter) {
                RandomDurationFilter.ALL -> { /* Pas de restriction */ }
                RandomDurationFilter.UNDER_2H -> {
                    if (movie.durationMinutes > 120) return@filter false
                }
                RandomDurationFilter.OVER_2H -> {
                    if (movie.durationMinutes <= 120) return@filter false
                }
            }

            // Fallback max duration if specified
            if (maxDuration != null && movie.durationMinutes > maxDuration) return@filter false

            // Streaming platform filter
            if (streaming != null && !movie.streamingServices.contains(streaming)) return@filter false

            // Target Audience Matching (C+A+S, C+S, A+C, etc.)
            when (movie.targetAudienceType) {
                "FAMILY" -> true // Family movies are valid for all combinations
                "SOLO" -> {
                    // Solo movie is eligible only if the sole viewer matches the owner
                    viewers.size == 1 && viewers.contains(movie.addedByMemberId)
                }
                "GROUP" -> {
                    // Movie is eligible if all selected viewers are allowed
                    val allowed = movie.allowedMemberIds.split(",").toSet()
                    if (allowed.contains("all")) true
                    else viewers.all { allowed.contains(it) }
                }
                else -> true
            }
        }
    }

    fun spinRandomSelector() {
        if (_isSpinning.value) return

        val candidates = getEligibleRandomMovies()
        if (candidates.isEmpty()) {
            _spinErrorMessage.value = "Aucun film ne correspond à cette configuration de spectateurs. Ajustez vos filtres ou ajoutez-en !"
            _winningMovie.value = null
            return
        }

        _spinErrorMessage.value = null
        _isSpinning.value = true
        _winningMovie.value = null

        spinJob?.cancel()
        spinJob = viewModelScope.launch {
            val winner = candidates[Random.nextInt(candidates.size)]
            val totalSteps = 24
            var currentDelay = 40L
            val shuffled = (candidates + candidates + candidates).shuffled()

            for (step in 0 until totalSteps) {
                val movieToShow = if (step == totalSteps - 1) winner else shuffled[step % shuffled.size]
                _spinningDisplayMovie.value = movieToShow
                delay(currentDelay)
                if (step > 14) currentDelay += 25L
                else if (step > 8) currentDelay += 10L
            }

            _spinningDisplayMovie.value = winner
            _winningMovie.value = winner
            _isSpinning.value = false
        }
    }

    fun clearWinner() {
        _winningMovie.value = null
        _spinningDisplayMovie.value = null
    }

    // Online Search
    fun onOnlineQueryChanged(query: String) {
        _onlineQuery.value = query
        searchJob?.cancel()

        if (query.trim().length < 2) {
            _onlineSearchResults.value = emptyList()
            _isOnlineSearching.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(400)
            _isOnlineSearching.value = true
            val results = repository.searchOnlineMovies(query)
            _onlineSearchResults.value = results
            _isOnlineSearching.value = false
        }
    }

    fun selectOnlineMovie(movie: MovieSearchResult) {
        _selectedOnlineMovie.value = movie
    }

    fun clearSelectedOnlineMovie() {
        _selectedOnlineMovie.value = null
    }

    fun togglePrivateNicheFilter() {
        val current = _filterState.value.showOnlyPrivateNiche
        _filterState.value = _filterState.value.copy(showOnlyPrivateNiche = !current)
    }

    // Add Movie
    fun addMovieToWishlist(
        title: String,
        originalTitle: String = "",
        posterUrl: String = "",
        year: Int = 2024,
        durationMinutes: Int = 100,
        genres: String = "Famille",
        synopsis: String = "",
        director: String = "",
        rating: Float = 4.5f,
        contentRating: String = "Tous publics",
        addedBy: FamilyMember? = null,
        targetAudienceType: String = "FAMILY",
        allowedMemberIds: List<String> = emptyList(),
        streamingServices: List<String> = listOf("Netflix"),
        userNotes: String = "",
        isPrivate: Boolean = false,
        notifyFamily: Boolean = true
    ) {
        viewModelScope.launch {
            val member = addedBy ?: _activeMember.value ?: allMembers.value.firstOrNull() ?: FamilyMember(
                id = "famille",
                name = "Famille",
                avatarEmoji = "🎬"
            )

            val allowedStr = if (targetAudienceType == "FAMILY") "all" else allowedMemberIds.joinToString(",")
            val streamingStr = streamingServices.joinToString(", ")

            val movie = Movie(
                id = "movie_" + UUID.randomUUID().toString().take(8),
                title = title.trim(),
                originalTitle = originalTitle.trim(),
                posterUrl = posterUrl.trim(),
                year = year,
                durationMinutes = durationMinutes,
                genres = genres.trim(),
                synopsis = synopsis.trim(),
                director = director.trim(),
                rating = rating,
                contentRating = contentRating,
                addedByMemberId = member.id,
                addedByMemberName = member.name,
                addedByMemberEmoji = member.avatarEmoji,
                addedTimestamp = System.currentTimeMillis(),
                isWatched = false,
                userNotes = userNotes.trim(),
                familyVotes = 1,
                targetAudienceType = targetAudienceType,
                allowedMemberIds = allowedStr,
                streamingServices = streamingStr,
                reviewsJson = "",
                isPrivate = isPrivate
            )

            repository.insertMovie(movie, notifyFamily = notifyFamily)
            _selectedOnlineMovie.value = null
            _onlineQuery.value = ""
            _onlineSearchResults.value = emptyList()

            _userFeedback.value = if (isPrivate) {
                "« ${movie.title} » ajouté à votre liste perso privée 🔒 (non partagé)."
            } else if (notifyFamily) {
                "« ${movie.title} » ajouté et notification envoyée à la famille 🔔 !"
            } else {
                "« ${movie.title} » ajouté sans notification."
            }
        }
    }

    fun joinMovieFromNotification(movieId: String) {
        viewModelScope.launch {
            val member = _activeMember.value ?: return@launch
            repository.joinMovieViewers(movieId, member.id)
            _userFeedback.value = "Vous avez rejoint les spectateurs du film 🙋 !"
        }
    }

    // Reviews & Ratings
    fun addReview(movieId: String, rating: Int, comment: String) {
        viewModelScope.launch {
            val member = _activeMember.value ?: return@launch
            val review = Review(
                id = "rev_" + UUID.randomUUID().toString().take(6),
                authorId = member.id,
                authorName = member.name,
                authorEmoji = member.avatarEmoji,
                rating = rating,
                comment = comment.trim(),
                timestamp = System.currentTimeMillis()
            )
            repository.addReview(movieId, review)
            _userFeedback.value = "Votre avis (${rating}★) a été enregistré !"

            // Instantly update modal state if open
            if (_movieForDetail.value?.id == movieId) {
                val current = _movieForDetail.value
                current?.let {
                    val reviews = repository.getReviewsForMovie(it).toMutableList()
                    reviews.removeAll { r -> r.authorId == member.id }
                    reviews.add(0, review)
                    val avg = String.format(Locale.US, "%.1f", reviews.sumOf { r -> r.rating }.toFloat() / reviews.size).toFloat()
                    val json = repository.serializeReviews(reviews)
                    _movieForDetail.value = it.copy(rating = avg, reviewsJson = json)
                }
            }
        }
    }

    fun getReviewsForMovie(movie: Movie): List<Review> {
        return repository.getReviewsForMovie(movie)
    }

    // Movie Actions
    fun toggleWatched(movie: Movie) {
        viewModelScope.launch {
            val newStatus = !movie.isWatched
            repository.setWatched(movie.id, newStatus)
            _userFeedback.value = if (newStatus) "\"${movie.title}\" marqué comme vu !" else "Remis dans la liste à voir."
            if (_winningMovie.value?.id == movie.id) {
                _winningMovie.value = _winningMovie.value?.copy(isWatched = newStatus)
            }
            if (_movieForDetail.value?.id == movie.id) {
                _movieForDetail.value = _movieForDetail.value?.copy(isWatched = newStatus)
            }
        }
    }

    fun upvoteMovie(movie: Movie) {
        viewModelScope.launch {
            repository.upvoteMovie(movie.id)
            _userFeedback.value = "Vote ajouté pour \"${movie.title}\" !"
        }
    }

    fun isMemberInterested(movie: Movie, memberId: String): Boolean {
        return when (movie.targetAudienceType) {
            "FAMILY" -> true
            "SOLO" -> {
                val ids = movie.allowedMemberIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (ids.isNotEmpty() && ids.first() != "all") ids.contains(memberId)
                else movie.addedByMemberId == memberId
            }
            "GROUP" -> {
                if (movie.allowedMemberIds == "all") true
                else movie.allowedMemberIds.split(",").map { it.trim() }.contains(memberId)
            }
            else -> true
        }
    }

    fun getInterestedMembers(movie: Movie): List<FamilyMember> {
        val members = allMembers.value
        return when (movie.targetAudienceType) {
            "FAMILY" -> members
            "SOLO" -> {
                val ids = movie.allowedMemberIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                if (ids.isNotEmpty() && ids.first() != "all") {
                    members.filter { ids.contains(it.id) }
                } else {
                    members.filter { it.id == movie.addedByMemberId }
                }
            }
            "GROUP" -> {
                if (movie.allowedMemberIds == "all") members
                else {
                    val ids = movie.allowedMemberIds.split(",").map { it.trim() }.toSet()
                    members.filter { ids.contains(it.id) }
                }
            }
            else -> members
        }
    }

    fun toggleInterestedMember(movie: Movie, member: FamilyMember) {
        viewModelScope.launch {
            val allMem = allMembers.value
            val allMemberIds = allMem.map { it.id }.toSet()

            val currentInterested: Set<String> = when (movie.targetAudienceType) {
                "FAMILY" -> allMemberIds
                "SOLO" -> {
                    val ids = movie.allowedMemberIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    if (ids.isNotEmpty() && ids.first() != "all") ids.toSet()
                    else setOf(movie.addedByMemberId).filter { it.isNotEmpty() }.toSet()
                }
                "GROUP" -> {
                    if (movie.allowedMemberIds == "all") allMemberIds
                    else movie.allowedMemberIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()
                }
                else -> allMemberIds
            }

            val isCurrentlyInterested = currentInterested.contains(member.id)

            val updatedAudienceType: String
            val updatedAllowedStr: String
            val newFamilyVotes: Int

            if (isCurrentlyInterested) {
                // User wants to withdraw
                val remaining = currentInterested - member.id
                if (remaining.isEmpty()) {
                    _userFeedback.value = "Vous êtes le seul spectateur de ce film."
                    return@launch
                } else if (remaining.size == 1) {
                    updatedAudienceType = "SOLO"
                    updatedAllowedStr = remaining.first()
                    newFamilyVotes = (movie.familyVotes - 1).coerceAtLeast(1)
                } else {
                    updatedAudienceType = "GROUP"
                    updatedAllowedStr = remaining.joinToString(",")
                    newFamilyVotes = (movie.familyVotes - 1).coerceAtLeast(1)
                }
                _userFeedback.value = "${member.name} s'est retiré(e) des spectateurs de \"${movie.title}\"."
            } else {
                // User wants to add themselves (e.g. S joins A and C)
                val newSet = currentInterested + member.id
                if (allMemberIds.isNotEmpty() && newSet.containsAll(allMemberIds)) {
                    updatedAudienceType = "FAMILY"
                    updatedAllowedStr = "all"
                } else {
                    updatedAudienceType = "GROUP"
                    updatedAllowedStr = newSet.joinToString(",")
                }
                newFamilyVotes = movie.familyVotes + 1
                _userFeedback.value = "${member.name} a rejoint les spectateurs de \"${movie.title}\" !"
            }

            val updatedMovie = movie.copy(
                targetAudienceType = updatedAudienceType,
                allowedMemberIds = updatedAllowedStr,
                familyVotes = newFamilyVotes
            )

            repository.updateMovie(updatedMovie)

            if (_movieForDetail.value?.id == movie.id) {
                _movieForDetail.value = updatedMovie
            }
            if (_winningMovie.value?.id == movie.id) {
                _winningMovie.value = updatedMovie
            }
        }
    }

    fun deleteMovie(movie: Movie) {
        viewModelScope.launch {
            repository.deleteMovie(movie)
            if (_movieForDetail.value?.id == movie.id) _movieForDetail.value = null
            if (_winningMovie.value?.id == movie.id) _winningMovie.value = null
            _userFeedback.value = "\"${movie.title}\" supprimé."
        }
    }

    fun openMovieDetail(movie: Movie) {
        _movieForDetail.value = movie
    }

    fun closeMovieDetail() {
        _movieForDetail.value = null
    }

    // Member Management
    fun addNewMember(name: String, emoji: String, colorHex: Long, role: String, pinCode: String, genres: String) {
        viewModelScope.launch {
            val id = name.lowercase().replace(" ", "_").filter { it.isLetterOrDigit() || it == '_' }
            val member = FamilyMember(
                id = id.ifEmpty { "mem_" + UUID.randomUUID().toString().take(5) },
                name = name.trim(),
                avatarEmoji = emoji,
                colorHex = colorHex,
                role = role.trim().ifEmpty { "Cinéphile" },
                pinCode = pinCode.ifEmpty { "1234" },
                preferredGenres = genres.ifEmpty { "Animation, Comédie" }
            )
            repository.insertMember(member)
            _userFeedback.value = "Profil ${member.name} créé et sécurisé !"
        }
    }

    fun updateMemberByAdmin(
        memberId: String,
        newName: String,
        newRole: String,
        newPin: String,
        newGenres: String,
        newEmoji: String,
        newColorHex: Long
    ) {
        viewModelScope.launch {
            val currentList = allMembers.value
            val existing = currentList.find { it.id == memberId } ?: return@launch
            val updated = existing.copy(
                name = newName.trim().ifEmpty { existing.name },
                role = newRole.trim().ifEmpty { existing.role },
                pinCode = newPin.trim().ifEmpty { existing.pinCode },
                preferredGenres = newGenres.trim().ifEmpty { existing.preferredGenres },
                avatarEmoji = newEmoji,
                colorHex = newColorHex
            )
            repository.insertMember(updated)
            if (_activeMember.value?.id == memberId) {
                _activeMember.value = updated
            }
            if (_originalAdminSession.value?.id == memberId) {
                _originalAdminSession.value = updated
            }
            _userFeedback.value = "Profil et code PIN de ${updated.name} mis à jour !"
        }
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            repository.deleteMember(member)
            if (_activeMember.value?.id == member.id) {
                _activeMember.value = allMembers.value.firstOrNull { it.id != member.id }
            }
            _userFeedback.value = "${member.name} retiré."
        }
    }

    fun restoreMember(member: FamilyMember) {
        viewModelScope.launch {
            repository.restoreMember(member)
            _userFeedback.value = "Compte de ${member.name} restauré avec succès !"
        }
    }

    fun purgeDeletedMember(memberId: String) {
        viewModelScope.launch {
            repository.purgeDeletedMember(memberId)
            _userFeedback.value = "Compte définitivement purgé."
        }
    }

    fun clearFeedback() {
        _userFeedback.value = null
    }

    // In-App Update Management
    fun startDownloadUpdate(apkUrl: String, targetVersionCode: Int) {
        val downloader = updateDownloader
        if (downloader == null) {
            _userFeedback.value = "Gestionnaire de téléchargement indisponible."
            return
        }
        viewModelScope.launch {
            downloader.downloadApk(apkUrl, targetVersionCode) { state ->
                _downloadState.value = state
            }
        }
    }

    fun installDownloadedApk(apkFile: File): Boolean {
        val downloader = updateDownloader ?: return false
        val success = downloader.installApk(apkFile)
        if (!success && !downloader.canRequestInstalls()) {
            _downloadState.value = DownloadState.NeedsPermission(apkFile)
        }
        return success
    }

    fun openInstallPermissionSettings() {
        updateDownloader?.openInstallPermissionSettings()
    }

    fun dismissUpdateDialog() {
        _isUpdateDialogDismissed.value = true
    }

    fun showUpdateDialog() {
        _isUpdateDialogDismissed.value = false
    }

    fun resetDownloadState() {
        _downloadState.value = DownloadState.Idle
    }

    fun publishAppUpdate(
        versionCode: Int,
        versionName: String,
        releaseNotes: String,
        apkUrl: String,
        isMandatory: Boolean
    ) {
        viewModelScope.launch {
            val dateStr = java.text.SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE).format(java.util.Date())
            val info = AppUpdateInfo(
                latestVersionCode = versionCode,
                latestVersionName = versionName.trim().ifEmpty { "1.0" },
                releaseNotes = releaseNotes.trim(),
                apkUrl = apkUrl.trim(),
                releaseDate = dateStr,
                isMandatory = isMandatory,
                updatedAtMillis = System.currentTimeMillis()
            )
            repository.publishAppUpdate(info)
            _userFeedback.value = "Mise à jour v$versionName diffusée à toute la famille !"
        }
    }

    fun checkUpdateManually() {
        val update = appUpdateInfo.value
        if (update != null && update.latestVersionCode > currentVersionCode) {
            _isUpdateDialogDismissed.value = false
            _userFeedback.value = "Mise à jour v${update.latestVersionName} disponible !"
        } else {
            _userFeedback.value = "CinéFamille est à jour (v$currentVersionName)."
        }
    }

    fun scheduleMovieNight(
        movieId: String,
        movieTitle: String,
        moviePosterUrl: String,
        movieDuration: Int,
        dateMillis: Long,
        timeString: String,
        themeOrNotes: String,
        snacks: String,
        invitedMemberIds: List<String>
    ) {
        val currentMember = _activeMember.value
        val organizerId = currentMember?.id ?: "steph"
        val organizerName = currentMember?.name ?: "Steph"
        val organizerEmoji = currentMember?.avatarEmoji ?: "🎬"

        val night = MovieNight(
            id = "night_${System.currentTimeMillis()}",
            movieId = movieId,
            movieTitle = movieTitle,
            moviePosterUrl = moviePosterUrl,
            movieDurationMinutes = movieDuration,
            organizerMemberId = organizerId,
            organizerMemberName = organizerName,
            organizerMemberEmoji = organizerEmoji,
            dateTimeMillis = dateMillis,
            timeString = timeString.ifBlank { "20h30" },
            themeOrNotes = themeOrNotes,
            snacksDescription = snacks,
            invitedMemberIds = invitedMemberIds,
            attendingMemberIds = listOf(organizerId),
            declinedMemberIds = emptyList()
        )

        viewModelScope.launch {
            repository.scheduleMovieNight(night)
            _userFeedback.value = "Soirée « $movieTitle » planifiée avec succès !"
        }
    }

    fun rsvpMovieNight(nightId: String, attending: Boolean) {
        val currentMember = _activeMember.value ?: return
        viewModelScope.launch {
            repository.rsvpMovieNight(nightId, currentMember.id, attending)
            _userFeedback.value = if (attending) "Votre présence est confirmée ! 🍿" else "Réponse enregistrée (non disponible) ❌"
        }
    }

    fun deleteMovieNight(nightId: String) {
        viewModelScope.launch {
            repository.deleteMovieNight(nightId)
            _userFeedback.value = "Soirée annulée."
        }
    }

    fun forceCloudSync() {
        viewModelScope.launch {
            val res = repository.syncCloudNow()
            _userFeedback.value = res
        }
    }
}

class MovieViewModelFactory(
    private val repository: MovieRepository,
    private val sessionManager: SessionManager,
    private val updateDownloader: UpdateDownloader? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            return MovieViewModel(repository, sessionManager, updateDownloader) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
