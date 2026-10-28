package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import com.example.data.local.FamilyMember
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.example.data.local.AppDatabase
import com.example.data.local.SessionManager
import com.example.data.remote.UpdateDownloader
import com.example.data.repository.MovieRepository
import com.example.ui.components.ActiveMemberSelectorBar
import com.example.ui.components.MovieDetailDialog
import com.example.ui.components.UpdateDialog
import com.example.ui.components.UpdateNoticeBanner
import com.example.ui.screens.AddMovieScreen
import com.example.ui.screens.FamilyScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MovieListScreen
import com.example.ui.screens.RandomSelectorScreen
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MovieViewModel
import com.example.ui.viewmodel.MovieViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: MovieViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = AppDatabase.getDatabase(applicationContext, lifecycleScope)
        val repository = MovieRepository(applicationContext, database.movieDao(), database.familyMemberDao())
        val sessionManager = SessionManager(applicationContext)
        val updateDownloader = UpdateDownloader(applicationContext)
        val factory = MovieViewModelFactory(repository, sessionManager, updateDownloader)
        viewModel = ViewModelProvider(this, factory)[MovieViewModel::class.java]

        setContent {
            MyApplicationTheme {
                val activeMember by viewModel.activeMember.collectAsStateWithLifecycle()
                val allMembers by viewModel.allMembers.collectAsStateWithLifecycle()

                if (activeMember == null) {
                    LoginScreen(
                        members = allMembers,
                        onLoginSuccess = { viewModel.login(it) }
                    )
                } else {
                    MainAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun MainAppScreen(viewModel: MovieViewModel) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val snackbarHostState = remember { SnackbarHostState() }

    val allMovies by viewModel.allMovies.collectAsStateWithLifecycle()
    val filteredMovies by viewModel.filteredMovies.collectAsStateWithLifecycle()
    val allMembers by viewModel.allMembers.collectAsStateWithLifecycle()
    val activeMember by viewModel.activeMember.collectAsStateWithLifecycle()
    val filterState by viewModel.filterState.collectAsStateWithLifecycle()

    val pendingAuthMember by viewModel.pendingMemberForAuth.collectAsStateWithLifecycle()
    val pinAuthError by viewModel.pinAuthError.collectAsStateWithLifecycle()

    val selectedViewers by viewModel.selectedViewerIds.collectAsStateWithLifecycle()
    val randomMemberId by viewModel.randomMemberFilterId.collectAsStateWithLifecycle()
    val randomMaxDuration by viewModel.randomMaxDurationFilter.collectAsStateWithLifecycle()
    val randomStreaming by viewModel.randomStreamingFilter.collectAsStateWithLifecycle()
    val isSpinning by viewModel.isSpinning.collectAsStateWithLifecycle()
    val spinningMovie by viewModel.spinningDisplayMovie.collectAsStateWithLifecycle()
    val winningMovie by viewModel.winningMovie.collectAsStateWithLifecycle()
    val spinError by viewModel.spinErrorMessage.collectAsStateWithLifecycle()

    val onlineQuery by viewModel.onlineQuery.collectAsStateWithLifecycle()
    val isSearchingOnline by viewModel.isOnlineSearching.collectAsStateWithLifecycle()
    val onlineResults by viewModel.onlineSearchResults.collectAsStateWithLifecycle()
    val selectedOnlineMovie by viewModel.selectedOnlineMovie.collectAsStateWithLifecycle()

    val detailMovie by viewModel.movieForDetail.collectAsStateWithLifecycle()
    val userFeedback by viewModel.userFeedback.collectAsStateWithLifecycle()
    val originalAdminSession by viewModel.originalAdminSession.collectAsStateWithLifecycle()

    val appUpdateInfo by viewModel.appUpdateInfo.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()
    val isUpdateDialogDismissed by viewModel.isUpdateDialogDismissed.collectAsStateWithLifecycle()
    val isUpdateAvailable = appUpdateInfo != null && appUpdateInfo!!.latestVersionCode > viewModel.currentVersionCode

    val recoverableMembers by viewModel.recoverableMembers.collectAsStateWithLifecycle()

    val isAdmin = activeMember?.isAdmin == true

    BackHandler(enabled = selectedTab != 0) {
        selectedTab = 0
    }

    LaunchedEffect(userFeedback) {
        userFeedback?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CinemaDarkBackground,
        topBar = {
            Column {
                ActiveMemberSelectorBar(
                    activeMember = activeMember,
                    allMembers = allMembers,
                    pendingMemberForAuth = pendingAuthMember,
                    pinAuthError = pinAuthError,
                    onRequestSelectMember = { viewModel.requestSelectMember(it) },
                    onSubmitPinAuth = { viewModel.submitPinAuth(it) },
                    onCancelPinAuth = { viewModel.cancelPinAuth() }
                )
                if (originalAdminSession != null && activeMember != null) {
                    ImpersonationStickyBanner(
                        admin = originalAdminSession!!,
                        actingAs = activeMember!!,
                        onExit = { viewModel.exitImpersonation() }
                    )
                }
                if (isUpdateAvailable && isUpdateDialogDismissed && appUpdateInfo != null) {
                    UpdateNoticeBanner(
                        versionName = appUpdateInfo!!.latestVersionName,
                        onOpenUpdate = { viewModel.showUpdateDialog() },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = CinemaDarkSurface,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                val unwatchedCount = allMovies.count { !it.isWatched }
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = {
                        BadgedBox(
                            badge = {
                                if (unwatchedCount > 0) {
                                    Badge(containerColor = CinemaGold, contentColor = Color.Black) {
                                        Text("$unwatchedCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        ) {
                            Icon(imageVector = if (selectedTab == 0) Icons.Default.Movie else Icons.Outlined.Movie, contentDescription = "Films")
                        }
                    },
                    label = { Text("Films", fontSize = 11.sp, fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CinemaGold,
                        indicatorColor = CinemaGold,
                        unselectedIconColor = CinemaTextSecondary,
                        unselectedTextColor = CinemaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_movies")
                )

                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = {
                        Icon(imageVector = if (selectedTab == 1) Icons.Default.Casino else Icons.Outlined.Casino, contentDescription = "Tirage")
                    },
                    label = { Text("Tirage", fontSize = 11.sp, fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CinemaGold,
                        indicatorColor = CinemaGold,
                        unselectedIconColor = CinemaTextSecondary,
                        unselectedTextColor = CinemaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_roulette")
                )

                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = {
                        Icon(imageVector = if (selectedTab == 2) Icons.Default.AddCircle else Icons.Outlined.AddCircleOutline, contentDescription = "Ajouter")
                    },
                    label = { Text("Ajouter", fontSize = 11.sp, fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CinemaGold,
                        indicatorColor = CinemaGold,
                        unselectedIconColor = CinemaTextSecondary,
                        unselectedTextColor = CinemaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_add")
                )

                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = {
                        if (isAdmin) {
                            Icon(imageVector = if (selectedTab == 3) Icons.Default.Group else Icons.Outlined.Group, contentDescription = "Admin")
                        } else {
                            Icon(imageVector = if (selectedTab == 3) Icons.Default.Person else Icons.Outlined.Person, contentDescription = "Profil")
                        }
                    },
                    label = {
                        Text(
                            text = if (isAdmin) "Admin" else "Profil",
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == 3) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.Black,
                        selectedTextColor = CinemaGold,
                        indicatorColor = CinemaGold,
                        unselectedIconColor = CinemaTextSecondary,
                        unselectedTextColor = CinemaTextSecondary
                    ),
                    modifier = Modifier.testTag("nav_tab_family")
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = CinemaDarkSurface,
                        contentColor = CinemaTextPrimary,
                        actionColor = CinemaGold,
                        shape = RoundedCornerShape(14.dp)
                    )
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                0 -> MovieListScreen(
                    movies = filteredMovies,
                    members = allMembers,
                    activeMember = activeMember,
                    filterState = filterState,
                    onSearchChange = { viewModel.setSearchQuery(it) },
                    onMemberFilterChange = { viewModel.setMemberFilter(it) },
                    onGenreFilterChange = { viewModel.setGenreFilter(it) },
                    onDurationFilterChange = { viewModel.setDurationFilter(it) },
                    onStatusFilterChange = { viewModel.setWatchStatusFilter(it) },
                    onAudienceFilterChange = { viewModel.setAudienceFilter(it) },
                    onStreamingFilterChange = { viewModel.setStreamingFilter(it) },
                    onResetFilters = { viewModel.resetFilters() },
                    onMovieClick = { viewModel.openMovieDetail(it) },
                    onToggleWatched = { viewModel.toggleWatched(it) },
                    onUpvote = { viewModel.upvoteMovie(it) },
                    onToggleInterested = { movie, member -> viewModel.toggleInterestedMember(movie, member) }
                )

                1 -> RandomSelectorScreen(
                    members = allMembers,
                    selectedViewerIds = selectedViewers,
                    selectedMemberId = randomMemberId,
                    selectedMaxDuration = randomMaxDuration,
                    selectedStreaming = randomStreaming,
                    eligibleMovies = viewModel.getEligibleRandomMovies(),
                    isSpinning = isSpinning,
                    spinningMovie = spinningMovie,
                    winningMovie = winningMovie,
                    errorMessage = spinError,
                    onToggleViewer = { viewModel.toggleViewer(it) },
                    onSelectAllViewers = { viewModel.selectAllViewers() },
                    onSelectMember = { viewModel.setRandomMemberFilter(it) },
                    onSelectDuration = { viewModel.setRandomMaxDurationFilter(it) },
                    onSelectStreaming = { viewModel.setRandomStreamingFilter(it) },
                    onSpin = { viewModel.spinRandomSelector() },
                    onReset = { viewModel.clearWinner() },
                    onMovieClick = { viewModel.openMovieDetail(it) },
                    onMarkWatched = { viewModel.toggleWatched(it) }
                )

                2 -> AddMovieScreen(
                    members = allMembers,
                    activeMember = activeMember,
                    onlineQuery = onlineQuery,
                    isSearching = isSearchingOnline,
                    searchResults = onlineResults,
                    selectedOnlineMovie = selectedOnlineMovie,
                    onQueryChange = { viewModel.onOnlineQueryChanged(it) },
                    onSelectOnlineMovie = { viewModel.selectOnlineMovie(it) },
                    onClearSelectedOnlineMovie = { viewModel.clearSelectedOnlineMovie() },
                    onAddMovie = { title, originalTitle, posterUrl, year, duration, genres, synopsis, director, rating, contentRating, addedBy, targetAudience, allowedMembers, streaming, notes ->
                        viewModel.addMovieToWishlist(
                            title = title,
                            originalTitle = originalTitle,
                            posterUrl = posterUrl,
                            year = year,
                            durationMinutes = duration,
                            genres = genres,
                            synopsis = synopsis,
                            director = director,
                            rating = rating,
                            contentRating = contentRating,
                            addedBy = addedBy,
                            targetAudienceType = targetAudience,
                            allowedMemberIds = allowedMembers,
                            streamingServices = streaming,
                            userNotes = notes
                        )
                        selectedTab = 0
                    }
                )

                3 -> FamilyScreen(
                    members = allMembers,
                    movies = allMovies,
                    activeMember = activeMember,
                    originalAdminSession = originalAdminSession,
                    appUpdateInfo = appUpdateInfo,
                    currentVersionCode = viewModel.currentVersionCode,
                    currentVersionName = viewModel.currentVersionName,
                    downloadState = downloadState,
                    recoverableMembers = recoverableMembers,
                    onRequestSelectMember = { viewModel.requestSelectMember(it) },
                    onAddNewMember = { name, emoji, color, role, pin, genres ->
                        viewModel.addNewMember(name, emoji, color, role, pin, genres)
                    },
                    onUpdateMemberByAdmin = { memberId, name, role, pin, genres, emoji, color ->
                        viewModel.updateMemberByAdmin(memberId, name, role, pin, genres, emoji, color)
                    },
                    onImpersonateMember = { viewModel.impersonateMember(it) },
                    onExitImpersonation = { viewModel.exitImpersonation() },
                    onDeleteMember = { viewModel.deleteMember(it) },
                    onRestoreMember = { viewModel.restoreMember(it) },
                    onPurgeDeletedMember = { viewModel.purgeDeletedMember(it) },
                    onUpdateGenres = { viewModel.updateMemberPreferredGenres(it) },
                    onCheckUpdateManually = { viewModel.checkUpdateManually() },
                    onStartDownloadUpdate = { url, targetVersionCode ->
                        viewModel.startDownloadUpdate(url, targetVersionCode)
                    },
                    onInstallDownloadedApk = { apkFile ->
                        viewModel.installDownloadedApk(apkFile)
                    },
                    onPublishAppUpdate = { versionCode, versionName, releaseNotes, apkUrl, isMandatory ->
                        viewModel.publishAppUpdate(versionCode, versionName, releaseNotes, apkUrl, isMandatory)
                    },
                    onLogout = { viewModel.logout() }
                )
            }

            // Movie Detail Modal with Ratings & Reviews
            detailMovie?.let { movie ->
                MovieDetailDialog(
                    movie = movie,
                    activeMember = activeMember,
                    members = allMembers,
                    reviews = viewModel.getReviewsForMovie(movie),
                    onDismiss = { viewModel.closeMovieDetail() },
                    onToggleWatched = { viewModel.toggleWatched(it) },
                    onUpvote = { viewModel.upvoteMovie(it) },
                    onDelete = { viewModel.deleteMovie(it) },
                    onToggleInterested = { m, mem -> viewModel.toggleInterestedMember(m, mem) },
                    onAddReview = { rating, comment ->
                        viewModel.addReview(movie.id, rating, comment)
                    }
                )
            }

            // In-App Update Modal Dialog
            if (isUpdateAvailable && !isUpdateDialogDismissed && appUpdateInfo != null) {
                UpdateDialog(
                    updateInfo = appUpdateInfo!!,
                    currentVersionName = viewModel.currentVersionName,
                    downloadState = downloadState,
                    onStartDownload = { url, targetVersionCode ->
                        viewModel.startDownloadUpdate(url, targetVersionCode)
                    },
                    onInstallApk = { apkFile ->
                        viewModel.installDownloadedApk(apkFile)
                    },
                    onRequestPermission = {
                        viewModel.openInstallPermissionSettings()
                    },
                    onDismiss = {
                        viewModel.dismissUpdateDialog()
                    }
                )
            }
        }
    }
}

@Composable
fun ImpersonationStickyBanner(
    admin: FamilyMember,
    actingAs: FamilyMember,
    onExit: () -> Unit
) {
    Surface(
        color = CinemaGold,
        tonalElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_impersonation_sticky_banner")
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 14.dp, vertical = 7.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.15f),
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = actingAs.avatarEmoji, fontSize = 16.sp)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mode Admin : ",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = "Profil ${actingAs.name}",
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = "Ajout de films, votes et avis pour ${actingAs.name}",
                        color = Color.Black.copy(alpha = 0.8f),
                        fontSize = 10.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black, contentColor = CinemaGold),
                shape = RoundedCornerShape(8.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.testTag("exit_impersonation_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = null,
                    tint = CinemaGold,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Retour à ${admin.name}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
