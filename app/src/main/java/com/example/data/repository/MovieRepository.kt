package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.local.FamilyMember
import com.example.data.local.FamilyMemberDao
import com.example.data.local.Movie
import com.example.data.local.MovieDao
import com.example.data.model.DeletedMemberInfo
import com.example.data.model.MovieSearchResult
import com.example.data.model.Review
import com.example.data.remote.OmdbMovieDetail
import com.example.data.remote.ImdbSuggestionService
import com.example.data.remote.OmdbApiService
import com.example.data.remote.StreamingAvailabilityService
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class MovieRepository(
    private val context: Context,
    private val movieDao: MovieDao,
    private val memberDao: FamilyMemberDao,
    private val omdbApi: OmdbApiService = OmdbApiService.create(),
    private val imdbApi: ImdbSuggestionService = ImdbSuggestionService.create(),
    private val externalScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    val allMovies: Flow<List<Movie>> = movieDao.getAllMovies()
    val allMembers: Flow<List<FamilyMember>> = memberDao.getAllMembers()
    private val _deletedMembers = kotlinx.coroutines.flow.MutableStateFlow<List<DeletedMemberInfo>>(emptyList())
    val deletedMembers: kotlinx.coroutines.flow.StateFlow<List<DeletedMemberInfo>> = _deletedMembers
    private val _appUpdateInfo = kotlinx.coroutines.flow.MutableStateFlow<com.example.data.model.AppUpdateInfo?>(null)

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val reviewListType = Types.newParameterizedType(List::class.java, Review::class.java)
    private val reviewsAdapter = moshi.adapter<List<Review>>(reviewListType)

    // Firestore instance with named database ID
    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(FirebaseApp.getInstance(), dbId)
        } catch (e: Exception) {
            null
        }
    }

    init {
        startFirestoreSync()
    }

    private fun startFirestoreSync() {
        val db = firestore ?: return

        db.collection("movies").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            externalScope.launch {
                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val movie = Movie(
                        id = doc.id,
                        title = data["title"] as? String ?: "",
                        originalTitle = data["originalTitle"] as? String ?: "",
                        posterUrl = data["posterUrl"] as? String ?: "",
                        year = (data["year"] as? Number)?.toInt() ?: 2024,
                        durationMinutes = (data["durationMinutes"] as? Number)?.toInt() ?: 100,
                        genres = data["genres"] as? String ?: "Famille",
                        synopsis = data["synopsis"] as? String ?: "",
                        director = data["director"] as? String ?: "",
                        rating = (data["rating"] as? Number)?.toFloat() ?: 4.5f,
                        contentRating = data["contentRating"] as? String ?: "Tous publics",
                        addedByMemberId = data["addedByMemberId"] as? String ?: "",
                        addedByMemberName = data["addedByMemberName"] as? String ?: "",
                        addedByMemberEmoji = data["addedByMemberEmoji"] as? String ?: "🎬",
                        addedTimestamp = (data["addedTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                        isWatched = data["isWatched"] as? Boolean ?: false,
                        userNotes = data["userNotes"] as? String ?: "",
                        familyVotes = (data["familyVotes"] as? Number)?.toInt() ?: 1,
                        targetAudienceType = data["targetAudienceType"] as? String ?: "FAMILY",
                        allowedMemberIds = data["allowedMemberIds"] as? String ?: "all",
                        streamingServices = data["streamingServices"] as? String ?: "Netflix",
                        reviewsJson = data["reviewsJson"] as? String ?: ""
                    )
                    movieDao.insertMovie(movie)
                }
            }
        }

        db.collection("family_members").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            externalScope.launch {
                for (doc in snapshot.documents) {
                    val data = doc.data ?: continue
                    val member = FamilyMember(
                        id = doc.id,
                        name = data["name"] as? String ?: "",
                        avatarEmoji = data["avatarEmoji"] as? String ?: "🎬",
                        colorHex = (data["colorHex"] as? Number)?.toLong() ?: 0xFF448AFF,
                        role = data["role"] as? String ?: "Famille",
                        pinCode = data["pinCode"] as? String ?: "1234",
                        preferredGenres = data["preferredGenres"] as? String ?: "Animation, Comédie",
                        isAdmin = (data["isAdmin"] as? Boolean) ?: (doc.id == "steph"),
                        createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
                    )
                    memberDao.insertMember(member)
                }
            }
        }

        db.collection("deleted_family_members").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null) return@addSnapshotListener
            val list = mutableListOf<DeletedMemberInfo>()
            val now = System.currentTimeMillis()
            val expiredDocIds = mutableListOf<String>()

            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val deletedAt = (data["deletedAt"] as? Number)?.toLong() ?: (data["createdAt"] as? Number)?.toLong() ?: now

                // Check if older than 7 days (RETENTION_PERIOD_MILLIS)
                if (now - deletedAt >= DeletedMemberInfo.RETENTION_PERIOD_MILLIS) {
                    expiredDocIds.add(doc.id)
                    continue
                }

                val member = FamilyMember(
                    id = doc.id,
                    name = data["name"] as? String ?: "",
                    avatarEmoji = data["avatarEmoji"] as? String ?: "🎬",
                    colorHex = (data["colorHex"] as? Number)?.toLong() ?: 0xFF448AFF,
                    role = data["role"] as? String ?: "Famille",
                    pinCode = data["pinCode"] as? String ?: "1234",
                    preferredGenres = data["preferredGenres"] as? String ?: "Animation, Comédie",
                    isAdmin = (data["isAdmin"] as? Boolean) ?: false,
                    createdAt = (data["createdAt"] as? Number)?.toLong() ?: now
                )
                list.add(DeletedMemberInfo(member = member, deletedAt = deletedAt))
            }
            _deletedMembers.value = list

            // Purge expired records permanently from Firestore
            if (expiredDocIds.isNotEmpty()) {
                externalScope.launch {
                    for (docId in expiredDocIds) {
                        try {
                            db.collection("deleted_family_members").document(docId).delete()
                        } catch (_: Exception) {}
                    }
                }
            }
        }

        db.collection("app_config").document("version").addSnapshotListener { snapshot, error ->
            if (error != null || snapshot == null || !snapshot.exists()) return@addSnapshotListener
            val data = snapshot.data ?: return@addSnapshotListener
            val info = com.example.data.model.AppUpdateInfo(
                latestVersionCode = (data["latestVersionCode"] as? Number)?.toInt() ?: 1,
                latestVersionName = data["latestVersionName"] as? String ?: "1.0",
                releaseNotes = data["releaseNotes"] as? String ?: "",
                apkUrl = data["apkUrl"] as? String ?: "",
                releaseDate = data["releaseDate"] as? String ?: "",
                isMandatory = data["isMandatory"] as? Boolean ?: false,
                updatedAtMillis = (data["updatedAtMillis"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
            _appUpdateInfo.value = info
        }
    }

    val appUpdateInfo: kotlinx.coroutines.flow.StateFlow<com.example.data.model.AppUpdateInfo?> = _appUpdateInfo

    suspend fun publishAppUpdate(info: com.example.data.model.AppUpdateInfo) = withContext(Dispatchers.IO) {
        _appUpdateInfo.value = info
        try {
            val map = hashMapOf(
                "latestVersionCode" to info.latestVersionCode,
                "latestVersionName" to info.latestVersionName,
                "releaseNotes" to info.releaseNotes,
                "apkUrl" to info.apkUrl,
                "releaseDate" to info.releaseDate,
                "isMandatory" to info.isMandatory,
                "updatedAtMillis" to info.updatedAtMillis
            )
            firestore?.collection("app_config")?.document("version")?.set(map, SetOptions.merge())
        } catch (_: Exception) {}
    }

    suspend fun insertMovie(movie: Movie) = withContext(Dispatchers.IO) {
        movieDao.insertMovie(movie)
        try {
            val map = hashMapOf(
                "title" to movie.title,
                "originalTitle" to movie.originalTitle,
                "posterUrl" to movie.posterUrl,
                "year" to movie.year,
                "durationMinutes" to movie.durationMinutes,
                "genres" to movie.genres,
                "synopsis" to movie.synopsis,
                "director" to movie.director,
                "rating" to movie.rating,
                "contentRating" to movie.contentRating,
                "addedByMemberId" to movie.addedByMemberId,
                "addedByMemberName" to movie.addedByMemberName,
                "addedByMemberEmoji" to movie.addedByMemberEmoji,
                "addedTimestamp" to movie.addedTimestamp,
                "isWatched" to movie.isWatched,
                "userNotes" to movie.userNotes,
                "familyVotes" to movie.familyVotes,
                "targetAudienceType" to movie.targetAudienceType,
                "allowedMemberIds" to movie.allowedMemberIds,
                "streamingServices" to movie.streamingServices,
                "reviewsJson" to movie.reviewsJson
            )
            firestore?.collection("movies")?.document(movie.id)?.set(map, SetOptions.merge())
        } catch (e: Exception) {
            // Offline fallback
        }
    }

    suspend fun updateMovie(movie: Movie) = withContext(Dispatchers.IO) {
        movieDao.updateMovie(movie)
        try {
            firestore?.collection("movies")?.document(movie.id)?.update(
                mapOf(
                    "isWatched" to movie.isWatched,
                    "familyVotes" to movie.familyVotes,
                    "reviewsJson" to movie.reviewsJson,
                    "rating" to movie.rating,
                    "targetAudienceType" to movie.targetAudienceType,
                    "allowedMemberIds" to movie.allowedMemberIds
                )
            )
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun deleteMovie(movie: Movie) = withContext(Dispatchers.IO) {
        movieDao.deleteMovie(movie)
        try {
            firestore?.collection("movies")?.document(movie.id)?.delete()
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun setWatched(id: String, isWatched: Boolean) = withContext(Dispatchers.IO) {
        movieDao.setWatched(id, isWatched)
        try {
            firestore?.collection("movies")?.document(id)?.update("isWatched", isWatched)
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun upvoteMovie(id: String) = withContext(Dispatchers.IO) {
        movieDao.upvoteMovie(id)
        val movie = movieDao.getMovieById(id) ?: return@withContext
        try {
            firestore?.collection("movies")?.document(id)?.update("familyVotes", movie.familyVotes)
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun addReview(movieId: String, review: Review) = withContext(Dispatchers.IO) {
        val movie = movieDao.getMovieById(movieId) ?: return@withContext
        val currentReviews = getReviewsForMovie(movie).toMutableList()
        currentReviews.removeAll { it.authorId == review.authorId }
        currentReviews.add(0, review)

        val updatedJson = reviewsAdapter.toJson(currentReviews)
        val avgRating = if (currentReviews.isNotEmpty()) {
            val total = currentReviews.sumOf { it.rating }
            String.format(Locale.US, "%.1f", total.toFloat() / currentReviews.size).toFloat()
        } else movie.rating

        val updatedMovie = movie.copy(reviewsJson = updatedJson, rating = avgRating)
        movieDao.updateMovie(updatedMovie)

        try {
            firestore?.collection("movies")?.document(movieId)?.update(
                mapOf(
                    "reviewsJson" to updatedJson,
                    "rating" to avgRating
                )
            )
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun getReviewsForMovie(movie: Movie): List<Review> {
        if (movie.reviewsJson.isBlank()) return emptyList()
        return try {
            reviewsAdapter.fromJson(movie.reviewsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun serializeReviews(reviews: List<Review>): String {
        return try {
            reviewsAdapter.toJson(reviews)
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun insertMember(member: FamilyMember) = withContext(Dispatchers.IO) {
        memberDao.insertMember(member)
        try {
            val map = hashMapOf(
                "name" to member.name,
                "avatarEmoji" to member.avatarEmoji,
                "colorHex" to member.colorHex,
                "role" to member.role,
                "pinCode" to member.pinCode,
                "preferredGenres" to member.preferredGenres,
                "isAdmin" to member.isAdmin,
                "createdAt" to member.createdAt
            )
            firestore?.collection("family_members")?.document(member.id)?.set(map, SetOptions.merge())
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun deleteMember(member: FamilyMember) = withContext(Dispatchers.IO) {
        memberDao.deleteMember(member)
        val now = System.currentTimeMillis()
        val info = DeletedMemberInfo(member = member, deletedAt = now)
        _deletedMembers.value = (_deletedMembers.value.filter { it.member.id != member.id } + info)
        try {
            val map = hashMapOf(
                "name" to member.name,
                "avatarEmoji" to member.avatarEmoji,
                "colorHex" to member.colorHex,
                "role" to member.role,
                "pinCode" to member.pinCode,
                "preferredGenres" to member.preferredGenres,
                "isAdmin" to member.isAdmin,
                "createdAt" to member.createdAt,
                "deletedAt" to now
            )
            firestore?.collection("deleted_family_members")?.document(member.id)?.set(map, SetOptions.merge())
            firestore?.collection("family_members")?.document(member.id)?.delete()
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun restoreMember(member: FamilyMember) = withContext(Dispatchers.IO) {
        memberDao.insertMember(member)
        _deletedMembers.value = _deletedMembers.value.filter { it.member.id != member.id }
        try {
            val map = hashMapOf(
                "name" to member.name,
                "avatarEmoji" to member.avatarEmoji,
                "colorHex" to member.colorHex,
                "role" to member.role,
                "pinCode" to member.pinCode,
                "preferredGenres" to member.preferredGenres,
                "isAdmin" to member.isAdmin,
                "createdAt" to member.createdAt
            )
            firestore?.collection("family_members")?.document(member.id)?.set(map, SetOptions.merge())
            firestore?.collection("deleted_family_members")?.document(member.id)?.delete()
        } catch (e: Exception) {
            // Ignore
        }
    }

    suspend fun purgeDeletedMember(memberId: String) = withContext(Dispatchers.IO) {
        _deletedMembers.value = _deletedMembers.value.filter { it.member.id != memberId }
        try {
            firestore?.collection("deleted_family_members")?.document(memberId)?.delete()
        } catch (_: Exception) {}
    }

    // REAL MOVIE DATABASE (IMDb & OMDb) SEARCH ENGINE
    suspend fun searchOnlineMovies(query: String): List<MovieSearchResult> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        if (cleanQuery.isBlank()) return@withContext emptyList()

        val results = mutableListOf<MovieSearchResult>()

        // 1. Query OMDb API (Open Movie Database based on IMDb)
        try {
            val searchResponse = omdbApi.searchMovies(cleanQuery)
            if (searchResponse.response.equals("True", ignoreCase = true) && !searchResponse.search.isNullOrEmpty()) {
                coroutineScope {
                    // Fetch full detail for top 5 search results
                    val detailDeferreds = searchResponse.search.take(6).map { item ->
                        async {
                            val id = item.imdbId
                            if (id != null) {
                                try {
                                    omdbApi.getMovieDetailById(id)
                                } catch (e: Exception) {
                                    null
                                }
                            } else null
                        }
                    }
                    val details = detailDeferreds.awaitAll().filterNotNull()

                    for (det in details) {
                        val title = det.title ?: continue
                        val year = det.year?.take(4)?.toIntOrNull() ?: 2024
                        val duration = det.runtime?.replace("min", "")?.trim()?.toIntOrNull() ?: 105
                        val genres = mapGenres(det.genre ?: "Famille")
                        val director = det.director?.takeIf { it != "N/A" } ?: ""
                        val synopsis = det.plot?.takeIf { it != "N/A" } ?: "Film répertorié sur la base de données IMDb."
                        val rating = det.imdbRating?.toFloatOrNull()?.let { it / 2f } ?: 4.5f
                        val poster = if (det.poster != null && det.poster != "N/A") det.poster else ""
                        val contentRating = det.rated?.takeIf { it != "N/A" } ?: "Tous publics"

                        val streaming = StreamingAvailabilityService.detectStreamingProviders(title, genres, year)

                        results.add(
                            MovieSearchResult(
                                title = title,
                                originalTitle = title,
                                posterUrl = poster,
                                year = year,
                                durationMinutes = duration,
                                genre = genres,
                                synopsis = synopsis,
                                director = director,
                                contentRating = contentRating,
                                streamingServices = streaming
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            // Fallthrough to IMDb suggestions
        }

        // 2. Query IMDb Official Suggestion API if OMDb had few results
        if (results.size < 3) {
            try {
                val firstChar = cleanQuery.first().lowercaseChar().toString().filter { it.isLetterOrDigit() }.ifEmpty { "a" }
                val formattedQuery = cleanQuery.lowercase().replace(" ", "_").replace("'", "")
                val imdbResponse = imdbApi.getSuggestions(firstChar, formattedQuery)
                val items = imdbResponse.results.orEmpty().filter { it.type == "feature" || it.type == "movie" || it.year != null }

                for (item in items.take(5)) {
                    val title = item.title ?: continue
                    if (results.any { it.title.equals(title, ignoreCase = true) }) continue

                    val year = item.year ?: 2024
                    val poster = item.image?.imageUrl ?: ""
                    val director = item.stars ?: ""
                    val genres = "Cinéma, Famille"
                    val streaming = StreamingAvailabilityService.detectStreamingProviders(title, genres, year)

                    results.add(
                        MovieSearchResult(
                            title = title,
                            originalTitle = title,
                            posterUrl = poster,
                            year = year,
                            durationMinutes = 110,
                            genre = genres,
                            synopsis = "Fiche IMDb ($title) avec $director. Disponible sur les services de streaming détectés ci-dessous.",
                            director = director,
                            contentRating = "Tous publics",
                            streamingServices = streaming
                        )
                    )
                }
            } catch (e: Exception) {
                // Fallthrough to curated fallback
            }
        }

        if (results.isNotEmpty()) {
            results
        } else {
            getCuratedFallbackResults(cleanQuery)
        }
    }

    private fun mapGenres(raw: String): String {
        return raw.split(",")
            .map { it.trim() }
            .map { g ->
                val lower = g.lowercase(Locale.ROOT)
                when {
                    lower.contains("animation") || lower.contains("anime") -> "Animation"
                    lower.contains("comedy") || lower.contains("comédie") -> "Comédie"
                    lower.contains("adventure") || lower.contains("aventure") -> "Aventure"
                    lower.contains("sci-fi") || lower.contains("science") -> "Science-fiction"
                    lower.contains("action") -> "Action"
                    lower.contains("family") || lower.contains("famille") || lower.contains("kids") -> "Famille"
                    lower.contains("fantasy") || lower.contains("fantastique") -> "Fantastique"
                    lower.contains("drama") || lower.contains("drame") -> "Drame"
                    lower.contains("romance") -> "Romance"
                    lower.contains("music") || lower.contains("musique") -> "Musique"
                    lower.contains("horror") || lower.contains("horreur") -> "Horreur"
                    lower.contains("thriller") || lower.contains("suspense") -> "Thriller"
                    else -> g
                }
            }
            .distinct()
            .joinToString(", ")
    }

    private fun getCuratedFallbackResults(query: String): List<MovieSearchResult> {
        val q = query.lowercase(Locale.ROOT)
        val catalog = listOf(
            MovieSearchResult(
                title = "Ratatouille",
                posterUrl = "https://m.media-amazon.com/images/M/MV5BMTMzODU0NTkxMF5BMl5BanBnXkFtZTcwMjQ4MzMzMw@@._V1_.jpg",
                year = 2007,
                durationMinutes = 111,
                genre = "Animation, Comédie, Famille",
                synopsis = "Rémy est un jeune rat qui rêve de devenir un grand chef français dans un grand restaurant parisien.",
                director = "Brad Bird",
                contentRating = "Tous publics",
                streamingServices = listOf("Amazon Prime Video", "Gratuit")
            ),
            MovieSearchResult(
                title = "Inception",
                posterUrl = "https://m.media-amazon.com/images/M/MV5BMjAxMzY3NjcxNF5BMl5BanBnXkFtZTcwNTI5OTM0Mw@@._V1_.jpg",
                year = 2010,
                durationMinutes = 148,
                genre = "Science-fiction, Action",
                synopsis = "Dom Cobb est un voleur expérimenté dans l'art de l'extraction de secrets enfouis au plus profond du subconscient.",
                director = "Christopher Nolan",
                contentRating = "12+",
                streamingServices = listOf("Netflix")
            ),
            MovieSearchResult(
                title = "Le Roi Lion",
                posterUrl = "https://m.media-amazon.com/images/M/MV5BYTYxNGMyZTYtMjE3MS00MzNjLWFjNmUtMDExMDQxNmU3MTc3XkEyXkFqcGdeQXVyNjY5NDU4NzI@._V1_.jpg",
                year = 1994,
                durationMinutes = 88,
                genre = "Animation, Aventure",
                synopsis = "Le jeune lionceau Simba doit succéder à son père Mufasa sur la Terre des Lions.",
                director = "Roger Allers",
                contentRating = "Tous publics",
                streamingServices = listOf("Gratuit")
            )
        )
        return catalog.filter {
            it.title.lowercase(Locale.ROOT).contains(q) ||
            it.genre.lowercase(Locale.ROOT).contains(q)
        }.ifEmpty {
            listOf(
                MovieSearchResult(
                    title = query.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() },
                    posterUrl = "https://images.unsplash.com/photo-1489599849927-2ee91cede3ba?w=600&auto=format&fit=crop&q=80",
                    year = 2024,
                    durationMinutes = 110,
                    genre = "Famille",
                    synopsis = "Fiche prête à enrichir pour '$query' dans la base de données de cinéma.",
                    director = "Cinéma",
                    contentRating = "Tous publics",
                    streamingServices = listOf("Netflix")
                )
            )
        }
    }
}
