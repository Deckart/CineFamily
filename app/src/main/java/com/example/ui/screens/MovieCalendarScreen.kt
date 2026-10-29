package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FamilyMember
import com.example.data.local.Movie
import com.example.data.model.MovieNight
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaDarkSurfaceVariant
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun MovieCalendarScreen(
    movieNights: List<MovieNight>,
    allMovies: List<Movie>,
    allMembers: List<FamilyMember>,
    activeMember: FamilyMember?,
    onScheduleNight: (
        movieId: String,
        movieTitle: String,
        moviePosterUrl: String,
        movieDuration: Int,
        dateMillis: Long,
        timeString: String,
        themeOrNotes: String,
        snacks: String,
        invitedMemberIds: List<String>
    ) -> Unit,
    onRsvp: (nightId: String, attending: Boolean) -> Unit,
    onDeleteNight: (nightId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0: À venir, 1: Passées
    var showScheduleDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val upcomingNights = remember(movieNights) {
        movieNights.filter { it.isUpcoming }.sortedBy { it.dateTimeMillis }
    }
    val pastNights = remember(movieNights) {
        movieNights.filter { !it.isUpcoming }.sortedByDescending { it.dateTimeMillis }
    }
    val currentList = if (selectedTab == 0) upcomingNights else pastNights

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Soirées Ciné 🍿",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    )
                    Text(
                        text = "Calendrier partagé de la famille",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary
                    )
                }

                Button(
                    onClick = { showScheduleDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CinemaGold,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("btn_open_schedule_dialog")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Planifier", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            // Tabs: À venir vs Passées
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CinemaDarkSurface,
                contentColor = CinemaGold,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = CinemaGold
                    )
                }
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            "À venir (${upcomingNights.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 0) CinemaGold else CinemaTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_upcoming_nights")
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            "Passées (${pastNights.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == 1) CinemaGold else CinemaTextSecondary
                        )
                    },
                    modifier = Modifier.testTag("tab_past_nights")
                )
            }

            // Nights list
            if (currentList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("📅", fontSize = 54.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (selectedTab == 0) "Aucune soirée ciné planifiée" else "Aucune soirée passée",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = CinemaTextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (selectedTab == 0)
                                "Choisissez un film de la liste et invitez la famille pour votre prochaine séance !"
                            else "Les soirées terminées apparaîtront ici.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = CinemaTextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (selectedTab == 0) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { showScheduleDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CinemaGold,
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier.testTag("btn_empty_schedule")
                            ) {
                                Text("Proposer une soirée ciné", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(currentList, key = { it.id }) { night ->
                        MovieNightCard(
                            night = night,
                            activeMember = activeMember,
                            allMembers = allMembers,
                            onRsvp = { attending -> onRsvp(night.id, attending) },
                            onDelete = { onDeleteNight(night.id) },
                            onExportToCalendar = {
                                exportToPhoneCalendar(context, night)
                            }
                        )
                    }
                }
            }
        }

        // Floating Action Button for planning
        FloatingActionButton(
            onClick = { showScheduleDialog = true },
            containerColor = CinemaGold,
            contentColor = Color.Black,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_schedule_night")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Planifier une soirée")
        }
    }

    if (showScheduleDialog) {
        ScheduleMovieNightDialog(
            allMovies = allMovies,
            allMembers = allMembers,
            activeMember = activeMember,
            onDismiss = { showScheduleDialog = false },
            onConfirm = { movieId, movieTitle, moviePosterUrl, movieDuration, dateMillis, timeString, theme, snacks, invitedIds ->
                onScheduleNight(movieId, movieTitle, moviePosterUrl, movieDuration, dateMillis, timeString, theme, snacks, invitedIds)
                showScheduleDialog = false
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieNightCard(
    night: MovieNight,
    activeMember: FamilyMember?,
    allMembers: List<FamilyMember>,
    onRsvp: (Boolean) -> Unit,
    onDelete: () -> Unit,
    onExportToCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatted = remember(night.dateTimeMillis) {
        val sdf = SimpleDateFormat("EEEE d MMMM yyyy", Locale.FRANCE)
        sdf.format(Date(night.dateTimeMillis)).replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.FRANCE) else it.toString() }
    }

    val isUserAttending = activeMember != null && night.attendingMemberIds.contains(activeMember.id)
    val isUserDeclined = activeMember != null && night.declinedMemberIds.contains(activeMember.id)
    val isOrganizerOrAdmin = activeMember != null && (activeMember.id == night.organizerMemberId || activeMember.isAdmin)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("movie_night_card_${night.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaDarkSurfaceVariant),
        border = BorderStroke(1.dp, if (isUserAttending) CinemaGold.copy(alpha = 0.5f) else Color(0xFF2C2C36))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Date & Organizer & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Event,
                        contentDescription = null,
                        tint = CinemaGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$dateFormatted • ${night.timeString}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = CinemaGold
                    )
                }

                if (isOrganizerOrAdmin) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_delete_night_${night.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Annuler la soirée",
                            tint = Color.Red.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Movie Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Poster
                if (night.moviePosterUrl.isNotBlank()) {
                    AsyncImage(
                        model = night.moviePosterUrl,
                        contentDescription = night.movieTitle,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(width = 65.dp, height = 95.dp)
                            .clip(RoundedCornerShape(10.dp))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(width = 65.dp, height = 95.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CinemaDarkSurface),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Movie,
                            contentDescription = null,
                            tint = CinemaTextSecondary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = night.movieTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = CinemaTextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Organisé par ${night.organizerMemberEmoji} ${night.organizerMemberName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = CinemaTextSecondary
                    )

                    if (night.themeOrNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "🎯 ${night.themeOrNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64B5F6),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (night.snacksDescription.isNotBlank()) {
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "🍿 ${night.snacksDescription}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFFB74D),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Invitations & RSVP list
            Text(
                text = "Participants (${night.attendingMemberIds.size} confirmé${if (night.attendingMemberIds.size > 1) "s" else ""})",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = CinemaTextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                allMembers.forEach { member ->
                    val isAttending = night.attendingMemberIds.contains(member.id)
                    val isDeclined = night.declinedMemberIds.contains(member.id)
                    val isInvited = night.invitedMemberIds.isEmpty() || night.invitedMemberIds.contains(member.id)

                    if (isInvited) {
                        val bgColor = when {
                            isAttending -> Color(0xFF1B5E20)
                            isDeclined -> Color(0xFF4A1414)
                            else -> Color(0xFF23232D)
                        }
                        val textColor = when {
                            isAttending -> Color(0xFF81C784)
                            isDeclined -> Color(0xFFE57373)
                            else -> CinemaTextSecondary
                        }
                        val statusEmoji = when {
                            isAttending -> "✅"
                            isDeclined -> "❌"
                            else -> "⏳"
                        }

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(bgColor)
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = member.avatarEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = member.name,
                                fontSize = 11.sp,
                                fontWeight = if (isAttending) FontWeight.Bold else FontWeight.Normal,
                                color = textColor
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = statusEmoji, fontSize = 10.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Interactive RSVP Buttons for active member
            if (activeMember != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { onRsvp(true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_rsvp_yes_${night.id}"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isUserAttending) Color(0xFF2E7D32) else Color(0xFF1E2820),
                            contentColor = if (isUserAttending) Color.White else Color(0xFF81C784)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isUserAttending) BorderStroke(1.dp, Color(0xFF66BB6A)) else null
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUserAttending) "Je participe 🍿" else "Participer",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = { onRsvp(false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("btn_rsvp_no_${night.id}"),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = if (isUserDeclined) Color(0xFFE57373) else CinemaTextSecondary
                        ),
                        border = BorderStroke(1.dp, if (isUserDeclined) Color(0xFFE57373) else Color(0xFF383842)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isUserDeclined) "Non dispo ❌" else "Pas dispo",
                            fontSize = 12.sp
                        )
                    }

                    // Export to Android Calendar button
                    IconButton(
                        onClick = onExportToCalendar,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaDarkSurface)
                            .testTag("btn_export_cal_${night.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "Ajouter à mon agenda",
                            tint = CinemaGold,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScheduleMovieNightDialog(
    allMovies: List<Movie>,
    allMembers: List<FamilyMember>,
    activeMember: FamilyMember?,
    onDismiss: () -> Unit,
    onConfirm: (
        movieId: String,
        movieTitle: String,
        moviePosterUrl: String,
        movieDuration: Int,
        dateMillis: Long,
        timeString: String,
        theme: String,
        snacks: String,
        invitedIds: List<String>
    ) -> Unit
) {
    var selectedMovie by remember { mutableStateOf(allMovies.firstOrNull { !it.isWatched } ?: allMovies.firstOrNull()) }
    var movieSearchQuery by remember { mutableStateOf("") }

    val cal = remember { Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, 1); set(Calendar.HOUR_OF_DAY, 20); set(Calendar.MINUTE, 30) } }
    var selectedDateMillis by remember { mutableLongStateOf(cal.timeInMillis) }
    var timeString by remember { mutableStateOf("20h30") }
    var themeOrNotes by remember { mutableStateOf("") }
    var snacksDescription by remember { mutableStateOf("Popcorn & friandises 🍿") }
    val invitedMembers = remember { mutableStateListOf<String>().apply { addAll(allMembers.map { it.id }) } }

    val filteredMovies = remember(allMovies, movieSearchQuery) {
        if (movieSearchQuery.isBlank()) allMovies
        else allMovies.filter { it.title.contains(movieSearchQuery, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CinemaDarkSurface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🎬", fontSize = 22.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Planifier une soirée ciné", color = CinemaGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Movie Selection
                Text("1. Choisir le film", style = MaterialTheme.typography.labelLarge, color = CinemaGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                if (selectedMovie != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = CinemaDarkSurfaceVariant),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CinemaGold)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (selectedMovie!!.posterUrl.isNotBlank()) {
                                AsyncImage(
                                    model = selectedMovie!!.posterUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(40.dp, 60.dp)
                                        .clip(RoundedCornerShape(6.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedMovie!!.title, fontWeight = FontWeight.Bold, color = CinemaTextPrimary, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${selectedMovie!!.year} • ${selectedMovie!!.durationMinutes} min", style = MaterialTheme.typography.bodySmall, color = CinemaTextSecondary)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = movieSearchQuery,
                    onValueChange = { movieSearchQuery = it },
                    label = { Text("Changer de film (recherche)") },
                    placeholder = { Text("Titre du film...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = Color(0xFF383842),
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_search_movie_for_night")
                )

                if (filteredMovies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        filteredMovies.take(4).forEach { movie ->
                            val isSelected = selectedMovie?.id == movie.id
                            Text(
                                text = movie.title,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.Black else CinemaTextPrimary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) CinemaGold else CinemaDarkSurfaceVariant)
                                    .border(1.dp, if (isSelected) CinemaGold else Color(0xFF383842), RoundedCornerShape(8.dp))
                                    .clickable { selectedMovie = movie }
                                    .padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Date & Time
                Text("2. Date et Heure", style = MaterialTheme.typography.labelLarge, color = CinemaGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val dateOptions = listOf(
                        "Ce soir" to 0,
                        "Demain" to 1,
                        "Ce samedi" to ((Calendar.SATURDAY - Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 7) % 7)
                    )

                    dateOptions.forEach { (label, daysAhead) ->
                        val optionCal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, daysAhead) }
                        val isPicked = (optionCal.get(Calendar.DAY_OF_YEAR) == Calendar.getInstance().apply { timeInMillis = selectedDateMillis }.get(Calendar.DAY_OF_YEAR))

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isPicked) CinemaGold else CinemaDarkSurfaceVariant)
                                .clickable {
                                    optionCal.set(Calendar.HOUR_OF_DAY, 20)
                                    optionCal.set(Calendar.MINUTE, 30)
                                    selectedDateMillis = optionCal.timeInMillis
                                }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPicked) Color.Black else CinemaTextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = timeString,
                    onValueChange = { timeString = it },
                    label = { Text("Heure de début") },
                    placeholder = { Text("ex. 20h30") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = Color(0xFF383842),
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_night_time")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Snacks & Theme
                Text("3. Détails & Convivialité", style = MaterialTheme.typography.labelLarge, color = CinemaGold, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = snacksDescription,
                    onValueChange = { snacksDescription = it },
                    label = { Text("Snacks & Boissons prévus 🍿") },
                    placeholder = { Text("Popcorn salé, nachos, glaces...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = Color(0xFF383842),
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_night_snacks")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = themeOrNotes,
                    onValueChange = { themeOrNotes = it },
                    label = { Text("Thème ou note pour la famille 🎯") },
                    placeholder = { Text("ex: Soirée pyjama, marathon saga...") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CinemaGold,
                        unfocusedBorderColor = Color(0xFF383842),
                        focusedTextColor = CinemaTextPrimary,
                        unfocusedTextColor = CinemaTextPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_night_theme")
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Invite Members
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("4. Inviter les membres", style = MaterialTheme.typography.labelLarge, color = CinemaGold, fontWeight = FontWeight.Bold)
                    TextButton(onClick = {
                        if (invitedMembers.size == allMembers.size) invitedMembers.clear()
                        else {
                            invitedMembers.clear()
                            invitedMembers.addAll(allMembers.map { it.id })
                        }
                    }) {
                        Text(if (invitedMembers.size == allMembers.size) "Désélectionner" else "Tous", fontSize = 11.sp, color = CinemaGold)
                    }
                }

                allMembers.forEach { member ->
                    val isChecked = invitedMembers.contains(member.id)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isChecked) invitedMembers.remove(member.id)
                                else invitedMembers.add(member.id)
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = isChecked,
                            onCheckedChange = { checked ->
                                if (checked) invitedMembers.add(member.id)
                                else invitedMembers.remove(member.id)
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = CinemaGold,
                                checkmarkColor = Color.Black,
                                uncheckedColor = CinemaTextSecondary
                            )
                        )
                        Text(member.avatarEmoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(member.name, color = CinemaTextPrimary, fontSize = 13.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val movie = selectedMovie ?: return@Button
                    onConfirm(
                        movie.id,
                        movie.title,
                        movie.posterUrl,
                        movie.durationMinutes,
                        selectedDateMillis,
                        timeString,
                        themeOrNotes,
                        snacksDescription,
                        invitedMembers.toList()
                    )
                },
                enabled = selectedMovie != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = CinemaGold,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("btn_confirm_schedule_night")
            ) {
                Text("Lancer l'invitation 💌", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler", color = CinemaTextSecondary)
            }
        }
    )
}

/**
 * Open standard Android system calendar to add the movie night event
 */
private fun exportToPhoneCalendar(context: Context, night: MovieNight) {
    try {
        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, "🍿 Soirée Ciné : ${night.movieTitle}")
            putExtra(
                CalendarContract.Events.DESCRIPTION,
                "Soirée film en famille organisée par ${night.organizerMemberName}.\n" +
                (if (night.themeOrNotes.isNotBlank()) "Thème: ${night.themeOrNotes}\n" else "") +
                (if (night.snacksDescription.isNotBlank()) "Snacks: ${night.snacksDescription}\n" else "")
            )
            putExtra(CalendarContract.Events.EVENT_LOCATION, "Salon familial")
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, night.dateTimeMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, night.dateTimeMillis + (night.movieDurationMinutes * 60 * 1000L))
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Impossible d'ouvrir le calendrier Android", android.widget.Toast.LENGTH_SHORT).show()
    }
}
