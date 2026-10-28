package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.VelvetRuby

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RandomSelectorScreen(
    members: List<FamilyMember>,
    selectedViewerIds: Set<String>,
    selectedMemberId: String?,
    selectedMaxDuration: Int?,
    selectedStreaming: String?,
    eligibleMovies: List<Movie>,
    isSpinning: Boolean,
    spinningMovie: Movie?,
    winningMovie: Movie?,
    errorMessage: String?,
    onToggleViewer: (String) -> Unit,
    onSelectAllViewers: () -> Unit,
    onSelectMember: (String?) -> Unit,
    onSelectDuration: (Int?) -> Unit,
    onSelectStreaming: (String?) -> Unit,
    onSpin: () -> Unit,
    onReset: () -> Unit,
    onMovieClick: (Movie) -> Unit,
    onMarkWatched: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.03f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spinRotation"
    )

    // Formula label: e.g. "C + A + S" or "C + S" or "A + C"
    val formulaLabel = members
        .filter { selectedViewerIds.contains(it.id) }
        .joinToString(" + ") { it.name.substringBefore(" ").take(1) }
        .ifEmpty { "Sélection" }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CinemaDarkBackground)
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Hero Header
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = CinemaGold.copy(alpha = 0.15f),
                    modifier = Modifier.size(50.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Casino,
                            contentDescription = null,
                            tint = CinemaGold,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Sélecteur Aléatoire de Soirée",
                    color = CinemaTextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Choisissez qui regarde ce soir, le sélecteur tire un film adapté !",
                    color = CinemaTextSecondary,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step 1: Qui participe à la soirée ? (C+A+S, C+S, A+C, etc.)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "1. Qui regarde ce soir ?",
                        color = CinemaGold,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CinemaGold
                    ) {
                        Text(
                            text = formulaLabel,
                            color = Color.Black,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Multi-select Viewer Chips
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    members.forEach { member ->
                        val isSelected = selectedViewerIds.contains(member.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = { onToggleViewer(member.id) },
                            leadingIcon = { Text(text = member.avatarEmoji) },
                            label = { Text(member.name, fontSize = 12.sp) },
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
                            ),
                            modifier = Modifier.testTag("roulette_viewer_${member.id}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quick combination buttons (C+A+S, C+S, A+C)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = CinemaDarkSurfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, CinemaDarkBorder),
                        modifier = Modifier
                            .clickable { onSelectAllViewers() }
                            .padding(vertical = 2.dp)
                    ) {
                        Text(
                            text = "Toute la famille",
                            color = CinemaGold,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Step 2: Filtres d'ajustement (Membre qui a ajouté, Durée, Streaming)
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "2. Proposé par :",
                    color = CinemaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterChip(
                            selected = selectedMemberId == null,
                            onClick = { onSelectMember(null) },
                            label = { Text("Toute la liste", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CinemaGold,
                                selectedLabelColor = Color.Black,
                                containerColor = CinemaDarkSurfaceVariant,
                                labelColor = CinemaTextPrimary
                            )
                        )
                    }
                    items(members) { member ->
                        val isSelected = selectedMemberId == member.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectMember(if (isSelected) null else member.id) },
                            leadingIcon = { Text(text = member.avatarEmoji, fontSize = 12.sp) },
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

                Spacer(modifier = Modifier.height(10.dp))

                // Streaming Platform filter
                Text(
                    text = "3. Plateforme disponible :",
                    color = CinemaTextSecondary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val streamList = listOf(null to "Toutes", "Netflix" to "Netflix", "Amazon Prime Video" to "Prime Video", "Gratuit" to "Gratuit")
                    streamList.forEach { (srv, label) ->
                        val isSelected = selectedStreaming == srv
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectStreaming(srv) },
                            label = { Text(label, fontSize = 10.sp) },
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

        Spacer(modifier = Modifier.height(12.dp))

        // Eligible Count Badge
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = CinemaDarkSurfaceVariant,
            border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎯", fontSize = 13.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${eligibleMovies.size} film${if (eligibleMovies.size > 1) "s" else ""} compatible${if (eligibleMovies.size > 1) "s" else ""} pour $formulaLabel",
                    color = CinemaTextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = VelvetRuby.copy(alpha = 0.15f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = errorMessage,
                    color = VelvetRuby,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Big Spin Action Button
        Button(
            onClick = onSpin,
            enabled = !isSpinning && eligibleMovies.isNotEmpty(),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .scale(if (isSpinning) 1f else pulseScale)
                .testTag("launch_roulette_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = CinemaGold,
                disabledContainerColor = CinemaDarkSurfaceVariant
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    tint = if (isSpinning || eligibleMovies.isEmpty()) CinemaTextSecondary else Color.Black,
                    modifier = Modifier
                        .size(22.dp)
                        .then(if (isSpinning) Modifier.rotate(spinRotation) else Modifier)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isSpinning) "Tirage en cours..." else "TIRER AU SORT POUR $formulaLabel",
                    color = if (isSpinning || eligibleMovies.isEmpty()) CinemaTextSecondary else Color.Black,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Winner or Reel Display
        if (isSpinning && spinningMovie != null) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
                border = androidx.compose.foundation.BorderStroke(2.dp, CinemaGold),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CinemaDarkSurfaceVariant)
                    ) {
                        AsyncImage(
                            model = spinningMovie.posterUrl,
                            contentDescription = spinningMovie.title,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = spinningMovie.title,
                        color = CinemaGold,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (winningMovie != null) {
            AnimatedVisibility(visible = true, enter = fadeIn() + scaleIn()) {
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CinemaDarkSurface),
                    border = androidx.compose.foundation.BorderStroke(2.dp, CinemaGold),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("winning_movie_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(shape = RoundedCornerShape(10.dp), color = CinemaGold) {
                            Text(
                                text = "🎉 FILM RETENU POUR $formulaLabel !",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Box(
                            modifier = Modifier
                                .width(135.dp)
                                .height(190.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(CinemaDarkSurfaceVariant)
                        ) {
                            if (winningMovie.posterUrl.isNotBlank()) {
                                AsyncImage(
                                    model = winningMovie.posterUrl,
                                    contentDescription = winningMovie.title,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(imageVector = Icons.Default.Movie, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(44.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = winningMovie.title,
                            color = CinemaTextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Streaming Platforms
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            winningMovie.streamingServices.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { srv ->
                                Surface(shape = RoundedCornerShape(6.dp), color = CinemaDarkSurfaceVariant) {
                                    Text(text = "📺 $srv", color = CinemaGold, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { onMarkWatched(winningMovie) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (winningMovie.isWatched) "Déjà noté vu !" else "On regarde ce film ce soir !",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { onMovieClick(winningMovie) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaGold)
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = CinemaGold, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Avis & Fiche", color = CinemaGold, fontSize = 11.sp)
                            }

                            OutlinedButton(
                                onClick = onSpin,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = CinemaTextPrimary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Relancer", color = CinemaTextPrimary, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
