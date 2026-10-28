package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.FamilyMember
import com.example.data.model.MovieSearchResult
import com.example.ui.theme.CinemaDarkBackground
import com.example.ui.theme.CinemaDarkBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaDarkSurfaceVariant
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextMuted
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddMovieScreen(
    members: List<FamilyMember>,
    activeMember: FamilyMember?,
    onlineQuery: String,
    isSearching: Boolean,
    searchResults: List<MovieSearchResult>,
    selectedOnlineMovie: MovieSearchResult?,
    onQueryChange: (String) -> Unit,
    onSelectOnlineMovie: (MovieSearchResult) -> Unit,
    onClearSelectedOnlineMovie: () -> Unit,
    onAddMovie: (
        title: String,
        originalTitle: String,
        posterUrl: String,
        year: Int,
        durationMinutes: Int,
        genres: String,
        synopsis: String,
        director: String,
        rating: Float,
        contentRating: String,
        addedBy: FamilyMember?,
        targetAudienceType: String,
        allowedMemberIds: List<String>,
        streamingServices: List<String>,
        userNotes: String
    ) -> Unit,
    modifier: Modifier = Modifier
) {
    var manualMode by remember { mutableStateOf(false) }

    var selectedMember by remember(activeMember) { mutableStateOf(activeMember ?: members.firstOrNull()) }
    var userNoteText by remember { mutableStateOf("") }

    // Target audience: "FAMILY", "SOLO", "GROUP"
    var audienceType by remember { mutableStateOf("FAMILY") }
    val groupAllowedMembers = remember { mutableStateListOf<String>() }

    // Streaming Services selection
    val selectedStreamingServices = remember { mutableStateListOf("Netflix") }

    // Initialize streaming from search result if selected
    remember(selectedOnlineMovie) {
        selectedOnlineMovie?.let {
            selectedStreamingServices.clear()
            selectedStreamingServices.addAll(it.streamingServices)
        }
    }

    // Manual form
    var manualTitle by remember { mutableStateOf("") }
    var manualYear by remember { mutableStateOf("2024") }
    var manualDuration by remember { mutableStateOf("105") }
    var manualGenre by remember { mutableStateOf("Famille") }
    var manualDirector by remember { mutableStateOf("") }
    var manualSynopsis by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (manualMode) "Ajout Manuel" else "Ajouter un Film",
                color = CinemaTextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            OutlinedButton(
                onClick = {
                    manualMode = !manualMode
                    onClearSelectedOnlineMovie()
                },
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaGold)
            ) {
                Icon(
                    imageVector = if (manualMode) Icons.Default.Public else Icons.Default.Edit,
                    contentDescription = null,
                    modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (manualMode) "En ligne" else "Manuel",
                    fontSize = 11.sp
                )
            }
        }

        if (selectedOnlineMovie != null || manualMode) {
            // Confirmation & Audience Configuration Screen
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (selectedOnlineMovie != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { onClearSelectedOnlineMovie() }
                            .padding(vertical = 2.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Choisir un autre film", color = CinemaGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    // Selected movie preview
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, CinemaGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp)) {
                            Box(
                                modifier = Modifier
                                    .width(80.dp)
                                    .height(115.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(CinemaDarkSurfaceVariant)
                            ) {
                                if (selectedOnlineMovie.posterUrl.isNotBlank()) {
                                    AsyncImage(model = selectedOnlineMovie.posterUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(selectedOnlineMovie.title, color = CinemaTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${selectedOnlineMovie.year} • ${selectedOnlineMovie.durationMinutes} min • ${selectedOnlineMovie.genre}", color = CinemaGold, fontSize = 11.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(selectedOnlineMovie.synopsis, color = CinemaTextSecondary, fontSize = 11.sp, maxLines = 3, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                } else {
                    // Manual inputs
                    OutlinedTextField(
                        value = manualTitle,
                        onValueChange = { manualTitle = it },
                        label = { Text("Titre du film") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 1. Qui propose ce film ?
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("1. Proposé par :", color = CinemaGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(members) { member ->
                                val isSelected = selectedMember?.id == member.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedMember = member },
                                    leadingIcon = { Text(member.avatarEmoji) },
                                    label = { Text(member.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(member.colorHex),
                                        selectedLabelColor = Color.White,
                                        containerColor = CinemaDarkSurfaceVariant,
                                        labelColor = CinemaTextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Audience / Pour qui ? (Toute la famille, Solo, ou Duo / Groupe)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("2. Pour qui est ce film ? (Liste distincte ou partagée)", color = CinemaGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))

                        val audienceList = listOf(
                            "FAMILY" to "👨‍👩‍👧‍👦 Toute la famille",
                            "SOLO" to "🔒 Pour moi seul (Privé)",
                            "GROUP" to "👥 Duo ou groupe spécifique"
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            audienceList.forEach { (type, label) ->
                                val isSelected = audienceType == type
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) CinemaGold.copy(alpha = 0.2f) else CinemaDarkSurfaceVariant,
                                    border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, if (isSelected) CinemaGold else CinemaDarkBorder),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { audienceType = type }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) CinemaGold else CinemaTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }

                        // If GROUP selected, choose allowed members (e.g. C+S, A+C)
                        if (audienceType == "GROUP") {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Sélectionnez les personnes du foyer invitées :", color = CinemaTextSecondary, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                members.forEach { m ->
                                    val isChecked = groupAllowedMembers.contains(m.id)
                                    FilterChip(
                                        selected = isChecked,
                                        onClick = {
                                            if (isChecked) groupAllowedMembers.remove(m.id)
                                            else groupAllowedMembers.add(m.id)
                                        },
                                        leadingIcon = { Text(m.avatarEmoji) },
                                        label = { Text(m.name, fontSize = 11.sp) },
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
                }

                // 3. Plateformes de streaming disponibles
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("3. Disponible sur quelles plateformes ?", color = CinemaGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val availableStreamers = listOf("Netflix", "Amazon Prime Video", "Gratuit")
                            availableStreamers.forEach { srv ->
                                val isChecked = selectedStreamingServices.contains(srv)
                                FilterChip(
                                    selected = isChecked,
                                    onClick = {
                                        if (isChecked) selectedStreamingServices.remove(srv)
                                        else selectedStreamingServices.add(srv)
                                    },
                                    label = { Text(srv, fontSize = 11.sp) },
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
                    }
                }

                // 4. Note personnelle
                OutlinedTextField(
                    value = userNoteText,
                    onValueChange = { userNoteText = it },
                    label = { Text("Mot pour le foyer (optionnel)") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Submit Button
                Button(
                    onClick = {
                        val title = selectedOnlineMovie?.title ?: manualTitle
                        val originalTitle = selectedOnlineMovie?.originalTitle ?: manualTitle
                        val posterUrl = selectedOnlineMovie?.posterUrl ?: ""
                        val year = selectedOnlineMovie?.year ?: (manualYear.toIntOrNull() ?: 2024)
                        val duration = selectedOnlineMovie?.durationMinutes ?: (manualDuration.toIntOrNull() ?: 100)
                        val genres = selectedOnlineMovie?.genre ?: manualGenre
                        val synopsis = selectedOnlineMovie?.synopsis ?: manualSynopsis
                        val director = selectedOnlineMovie?.director ?: manualDirector

                        onAddMovie(
                            title,
                            originalTitle,
                            posterUrl,
                            year,
                            duration,
                            genres,
                            synopsis,
                            director,
                            4.5f,
                            "Tous publics",
                            selectedMember,
                            audienceType,
                            groupAllowedMembers.toList(),
                            selectedStreamingServices.toList(),
                            userNoteText
                        )
                        userNoteText = ""
                        manualTitle = ""
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("confirm_add_movie_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = CinemaGold),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.Black, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ENREGISTRER CE FILM DANS FIREBASE", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        } else {
            // Online search bar & results
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                OutlinedTextField(
                    value = onlineQuery,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Ex: Inception, Avatar, Retour vers le futur...", color = CinemaTextMuted) },
                    label = { Text("Rechercher dans la base IMDb") },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = CinemaGold) },
                    trailingIcon = {
                        if (isSearching) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = CinemaGold, strokeWidth = 2.dp)
                        } else if (onlineQuery.isNotEmpty()) {
                            IconButton(onClick = { onQueryChange("") }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = CinemaTextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
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
                        .padding(vertical = 6.dp)
                        .testTag("online_movie_search_input")
                )

                if (searchResults.isNotEmpty()) {
                    Text(
                        text = "${searchResults.size} films trouvés avec plateformes de streaming :",
                        color = CinemaTextSecondary,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { item ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectOnlineMovie(item) }
                            ) {
                                Row(modifier = Modifier.padding(10.dp)) {
                                    Box(
                                        modifier = Modifier
                                            .width(66.dp)
                                            .height(95.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CinemaDarkSurfaceVariant)
                                    ) {
                                        if (item.posterUrl.isNotBlank()) {
                                            AsyncImage(model = item.posterUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(item.title, color = CinemaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text("${item.year} • ${item.durationMinutes}m • ${item.genre}", color = CinemaGold, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        // Streaming badges
                                        FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            item.streamingServices.forEach { srv ->
                                                Surface(shape = RoundedCornerShape(4.dp), color = CinemaDarkSurfaceVariant) {
                                                    Text(text = srv, color = CinemaGold, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "Tapez un titre pour chercher en ligne et détecter les disponibilités streaming (Netflix, Prime Video, Gratuit).",
                            color = CinemaTextSecondary,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }
}
