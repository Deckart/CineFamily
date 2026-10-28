package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FamilyMember
import com.example.data.local.Movie
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaDarkBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaDarkSurfaceVariant
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextMuted
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.StarYellow
import com.example.ui.theme.VelvetRuby
import com.example.ui.viewmodel.DurationFilter
import com.example.ui.viewmodel.FilterState
import com.example.ui.viewmodel.WatchStatusFilter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieListScreen(
    movies: List<Movie>,
    members: List<FamilyMember>,
    activeMember: FamilyMember? = null,
    filterState: FilterState,
    onSearchChange: (String) -> Unit,
    onMemberFilterChange: (String?) -> Unit,
    onGenreFilterChange: (String?) -> Unit,
    onDurationFilterChange: (DurationFilter) -> Unit,
    onStatusFilterChange: (WatchStatusFilter) -> Unit,
    onAudienceFilterChange: (String?) -> Unit,
    onStreamingFilterChange: (String?) -> Unit,
    onResetFilters: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onToggleWatched: (Movie) -> Unit,
    onUpvote: (Movie) -> Unit,
    onToggleInterested: ((Movie, FamilyMember) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var showAdvancedFilters by remember { mutableStateOf(false) }

    val popularGenres = listOf(
        "Animation", "Comédie", "Aventure", "Science-fiction", "Famille", "Fantastique", "Action", "Drame"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
    ) {
        // Search Bar
        OutlinedTextField(
            value = filterState.searchQuery,
            onValueChange = onSearchChange,
            placeholder = { Text("Rechercher un film, réalisateur...", color = CinemaTextMuted) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = CinemaGold
                )
            },
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (filterState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer", tint = CinemaTextSecondary)
                        }
                    }
                    IconButton(
                        onClick = { showAdvancedFilters = !showAdvancedFilters },
                        modifier = Modifier.testTag("toggle_filters_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filtres avancés",
                            tint = if (showAdvancedFilters || filterState.genre != null || filterState.streamingService != null || filterState.audienceType != null) CinemaGold else CinemaTextSecondary
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = CinemaDarkSurface,
                unfocusedContainerColor = CinemaDarkSurface,
                focusedBorderColor = CinemaGold,
                unfocusedBorderColor = CinemaDarkBorder,
                focusedTextColor = CinemaTextPrimary,
                unfocusedTextColor = CinemaTextPrimary
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp)
                .testTag("movie_search_input")
        )

        // Audience Tabs: Toute la famille vs Solo vs Duo/Groupe
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val audienceOptions = listOf(
                null to "Tous les films",
                "FAMILY" to "👨‍👩‍👧‍👦 Toute la famille",
                "GROUP" to "👥 Duo / Groupe (C+S, A+C)",
                "SOLO" to "🔒 Solo / Privé"
            )
            items(audienceOptions) { (key, label) ->
                val isSelected = filterState.audienceType == key
                FilterChip(
                    selected = isSelected,
                    onClick = { onAudienceFilterChange(key) },
                    label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CinemaGold,
                        selectedLabelColor = Color.Black,
                        containerColor = CinemaDarkSurfaceVariant,
                        labelColor = CinemaTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) CinemaGold else CinemaDarkBorder,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Member Filters Horizontal Scroll
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                val isSelected = filterState.memberId == null
                FilterChip(
                    selected = isSelected,
                    onClick = { onMemberFilterChange(null) },
                    label = { Text("Tous les ajouts", fontSize = 11.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = CinemaGold,
                        selectedLabelColor = Color.Black,
                        containerColor = CinemaDarkSurfaceVariant,
                        labelColor = CinemaTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) CinemaGold else CinemaDarkBorder,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }

            items(members) { member ->
                val isSelected = filterState.memberId == member.id
                FilterChip(
                    selected = isSelected,
                    onClick = { onMemberFilterChange(if (isSelected) null else member.id) },
                    leadingIcon = { Text(text = member.avatarEmoji, fontSize = 13.sp) },
                    label = { Text(member.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(member.colorHex),
                        selectedLabelColor = Color.White,
                        containerColor = CinemaDarkSurfaceVariant,
                        labelColor = CinemaTextPrimary
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) Color(member.colorHex) else CinemaDarkBorder,
                        enabled = true,
                        selected = isSelected
                    )
                )
            }
        }

        // Expandable Filters: Streaming Platform & Genres
        AnimatedVisibility(visible = showAdvancedFilters) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .background(CinemaDarkSurface, RoundedCornerShape(16.dp))
                    .border(1.dp, CinemaDarkBorder, RoundedCornerShape(16.dp))
                    .padding(12.dp)
            ) {
                // Streaming services filter
                Text(
                    text = "Plateforme de streaming :",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val streamList = listOf(null to "Toutes", "Netflix" to "Netflix", "Amazon Prime Video" to "Prime Video", "Gratuit" to "Gratuit")
                    streamList.forEach { (srv, label) ->
                        val isSelected = filterState.streamingService == srv
                        FilterChip(
                            selected = isSelected,
                            onClick = { onStreamingFilterChange(srv) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGold,
                                selectedLabelColor = Color.Black,
                                containerColor = CinemaDarkSurfaceVariant,
                                labelColor = CinemaTextPrimary
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Genres Row
                Text(
                    text = "Genre du film :",
                    color = CinemaTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = filterState.genre == null,
                            onClick = { onGenreFilterChange(null) },
                            label = { Text("Tous genres", fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGold,
                                selectedLabelColor = Color.Black,
                                containerColor = CinemaDarkSurfaceVariant,
                                labelColor = CinemaTextPrimary
                            )
                        )
                    }
                    items(popularGenres) { genre ->
                        val isSelected = filterState.genre == genre
                        FilterChip(
                            selected = isSelected,
                            onClick = { onGenreFilterChange(if (isSelected) null else genre) },
                            label = { Text(genre, fontSize = 10.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGold,
                                selectedLabelColor = Color.Black,
                                containerColor = CinemaDarkSurfaceVariant,
                                labelColor = CinemaTextPrimary
                            )
                        )
                    }
                }
            }
        }

        // Status Tabs & Count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                WatchStatusFilter.values().forEach { status ->
                    val isSelected = filterState.status == status
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) CinemaGold else CinemaDarkSurfaceVariant,
                        modifier = Modifier.clickable { onStatusFilterChange(status) }
                    ) {
                        Text(
                            text = status.label,
                            color = if (isSelected) Color.Black else CinemaTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Text(
                text = "${movies.size} film${if (movies.size > 1) "s" else ""}",
                color = CinemaTextMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Movies List
        if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Aucun film trouvé",
                        color = CinemaTextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Modifiez les filtres (auditoire, membre, plateforme) ou ajoutez un nouveau film.",
                        color = CinemaTextSecondary,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CinemaGold,
                        modifier = Modifier.clickable { onResetFilters() }
                    ) {
                        Text(
                            text = "Réinitialiser les filtres",
                            color = Color.Black,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("movie_list")
            ) {
                items(movies, key = { it.id }) { movie ->
                    MovieItemCard(
                        movie = movie,
                        members = members,
                        activeMember = activeMember,
                        onClick = { onMovieClick(movie) },
                        onToggleWatched = { onToggleWatched(movie) },
                        onUpvote = { onUpvote(movie) },
                        onToggleInterested = onToggleInterested
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieItemCard(
    movie: Movie,
    members: List<FamilyMember> = emptyList(),
    activeMember: FamilyMember? = null,
    onClick: () -> Unit,
    onToggleWatched: () -> Unit,
    onUpvote: () -> Unit,
    onToggleInterested: ((Movie, FamilyMember) -> Unit)? = null
) {
    val allMemberIds = members.map { it.id }.toSet()
    val interestedMemberIds: Set<String> = when (movie.targetAudienceType) {
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
    val interestedMembers = members.filter { interestedMemberIds.contains(it.id) }
    val isUserInterested = activeMember != null && interestedMemberIds.contains(activeMember.id)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("movie_item_${movie.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Poster
            Box(
                modifier = Modifier
                    .width(82.dp)
                    .height(120.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(CinemaDarkSurfaceVariant)
            ) {
                if (movie.posterUrl.isNotBlank()) {
                    AsyncImage(
                        model = movie.posterUrl,
                        contentDescription = movie.title,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = CinemaGold.copy(alpha = 0.5f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                // Rating Badge
                Surface(
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    color = Color.Black.copy(alpha = 0.75f),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = StarYellow,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = "${movie.rating}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(
                modifier = Modifier
                    .weight(1f)
                    .height(120.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = movie.title,
                            color = CinemaTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Text(
                            text = "${movie.year}",
                            color = CinemaTextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Streaming & Audience Badges
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Streaming Service badge
                        val primaryStream = movie.streamingServices.split(",").firstOrNull()?.trim() ?: "Netflix"
                        val (sBg, sColor) = when {
                            primaryStream.contains("Netflix", ignoreCase = true) -> Color(0xFFE50914) to Color.White
                            primaryStream.contains("Prime", ignoreCase = true) -> Color(0xFF00A8E1) to Color.White
                            else -> Color(0xFF00C853) to Color.Black
                        }
                        Surface(shape = RoundedCornerShape(4.dp), color = sBg) {
                            Text(
                                text = primaryStream,
                                color = sColor,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }

                        // Audience pill
                        val audText = when (movie.targetAudienceType) {
                            "FAMILY" -> "Famille"
                            "SOLO" -> "Solo"
                            else -> {
                                val initials = interestedMembers.map { it.name.take(1).uppercase() }
                                if (initials.isNotEmpty()) initials.joinToString("+")
                                else movie.allowedMemberIds.split(",").map { it.take(1).uppercase() }.joinToString("+")
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = CinemaDarkSurfaceVariant
                        ) {
                            Text(
                                text = audText,
                                color = CinemaGold,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    val hours = movie.durationMinutes / 60
                    val mins = movie.durationMinutes % 60
                    val dur = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"

                    Text(
                        text = "$dur • ${movie.genres.split(",").firstOrNull()?.trim() ?: "Famille"}",
                        color = CinemaTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Bottom row: Added by tag + Interested emojis + Interest toggle + upvote & watched toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CinemaDarkSurfaceVariant
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = movie.addedByMemberEmoji, fontSize = 10.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = movie.addedByMemberName,
                                    color = CinemaTextPrimary,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        // Interested viewers emoji list
                        if (interestedMembers.isNotEmpty()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                interestedMembers.take(3).forEach { m ->
                                    Text(text = m.avatarEmoji, fontSize = 10.sp, modifier = Modifier.padding(end = 1.dp))
                                }
                            }
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Interest Toggle Button
                        if (activeMember != null && onToggleInterested != null) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isUserInterested) CinemaGold.copy(alpha = 0.25f) else CinemaDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (isUserInterested) CinemaGold else CinemaDarkBorder),
                                modifier = Modifier
                                    .clickable { onToggleInterested(movie, activeMember) }
                                    .testTag("quick_toggle_interest_${movie.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (isUserInterested) Icons.Default.Check else Icons.Default.Add,
                                        contentDescription = if (isUserInterested) "Retirer mon envie" else "Participer",
                                        tint = if (isUserInterested) CinemaGold else CinemaTextSecondary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(
                                        text = if (isUserInterested) "Moi ✓" else "+ Moi",
                                        color = if (isUserInterested) CinemaGold else CinemaTextSecondary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = CinemaDarkSurfaceVariant,
                            modifier = Modifier.clickable { onUpvote() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Favorite, contentDescription = null, tint = VelvetRuby, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${movie.familyVotes}", color = CinemaTextPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (movie.isWatched) Color(0xFF2E7D32) else CinemaDarkSurfaceVariant,
                            modifier = Modifier.clickable { onToggleWatched() }
                        ) {
                            Box(modifier = Modifier.size(24.dp), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (movie.isWatched) Icons.Default.Check else Icons.Outlined.CheckCircleOutline,
                                    contentDescription = null,
                                    tint = if (movie.isWatched) Color.White else CinemaTextSecondary,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
