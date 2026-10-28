package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.local.FamilyMember
import com.example.data.local.Movie
import com.example.data.model.Review
import com.example.ui.theme.CinemaDarkBorder
import com.example.ui.theme.CinemaDarkSurface
import com.example.ui.theme.CinemaDarkSurfaceVariant
import com.example.ui.theme.CinemaGold
import com.example.ui.theme.CinemaTextPrimary
import com.example.ui.theme.CinemaTextSecondary
import com.example.ui.theme.StarYellow
import com.example.ui.theme.VelvetRuby

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MovieDetailDialog(
    movie: Movie,
    activeMember: FamilyMember?,
    members: List<FamilyMember> = emptyList(),
    reviews: List<Review>,
    onDismiss: () -> Unit,
    onToggleWatched: (Movie) -> Unit,
    onUpvote: (Movie) -> Unit,
    onDelete: (Movie) -> Unit,
    onToggleInterested: (Movie, FamilyMember) -> Unit = { _, _ -> },
    onAddReview: (rating: Int, comment: String) -> Unit
) {
    var showReviewBottomSheet by remember { mutableStateOf(false) }

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

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(24.dp))
                .border(1.dp, CinemaDarkBorder, RoundedCornerShape(24.dp))
                .testTag("movie_detail_dialog"),
            color = CinemaDarkSurface
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                // Header with image
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(240.dp)
                ) {
                    if (movie.posterUrl.isNotBlank()) {
                        AsyncImage(
                            model = movie.posterUrl,
                            contentDescription = movie.title,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(240.dp)
                                .background(CinemaDarkSurfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Movie,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = CinemaGold.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // Scrim
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(240.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Transparent,
                                        CinemaDarkSurface
                                    )
                                )
                            )
                    )

                    // Close Button
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(12.dp)
                            .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            .testTag("close_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = Color.White
                        )
                    }

                    // Status pill overlay
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (movie.isWatched) Color(0xFF2E7D32) else CinemaGold,
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                text = if (movie.isWatched) "✓ Déjà vu" else "À voir",
                                color = if (movie.isWatched) Color.White else Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        // Audience Tag
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.7f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val audienceLabel = when (movie.targetAudienceType) {
                                    "FAMILY" -> "👨‍👩‍👧‍👦 Toute la famille"
                                    "SOLO" -> "🔒 Solo (${movie.addedByMemberName})"
                                    else -> {
                                        val names = interestedMembers.map { it.name.substringBefore(" ") }
                                        val str = if (names.isNotEmpty()) names.joinToString(" + ") else movie.allowedMemberIds.replace(",", "+")
                                        "👥 Groupe ($str)"
                                    }
                                }
                                Text(
                                    text = audienceLabel,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                // Content Body
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = movie.title,
                        color = CinemaTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Meta: Duration, Year, Director
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val hours = movie.durationMinutes / 60
                        val mins = movie.durationMinutes % 60
                        val durationText = if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = CinemaGold,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(durationText, color = CinemaTextSecondary, fontSize = 12.sp)
                        }

                        Text("•", color = CinemaTextSecondary)
                        Text("${movie.year}", color = CinemaTextSecondary, fontSize = 12.sp)

                        if (movie.director.isNotBlank()) {
                            Text("•", color = CinemaTextSecondary)
                            Text("De: ${movie.director}", color = CinemaTextSecondary, fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Streaming Services Badges
                    Text(
                        text = "Disponible sur :",
                        color = CinemaTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        movie.streamingServices.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { service ->
                            val (badgeBg, badgeText) = when {
                                service.contains("Netflix", ignoreCase = true) -> Color(0xFFE50914) to Color.White
                                service.contains("Prime", ignoreCase = true) -> Color(0xFF00A8E1) to Color.White
                                else -> Color(0xFF00C853) to Color.Black
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = badgeBg
                            ) {
                                Text(
                                    text = service,
                                    color = badgeText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Genres
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        movie.genres.split(",").map { it.trim() }.filter { it.isNotEmpty() }.forEach { genre ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CinemaDarkSurfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder)
                            ) {
                                Text(
                                    text = genre,
                                    color = CinemaGold,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Added by Card
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaDarkSurfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = movie.addedByMemberEmoji, fontSize = 22.sp)
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Ajouté par", color = CinemaTextSecondary, fontSize = 11.sp)
                                Text(movie.addedByMemberName, color = CinemaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { onUpvote(movie) }) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Favorite, contentDescription = null, tint = VelvetRuby, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${movie.familyVotes}", color = CinemaTextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                            }
                        }
                    }

                    if (movie.userNotes.isNotBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "💡 Mot de ${movie.addedByMemberName} : \"${movie.userNotes}\"",
                            color = CinemaGold,
                            fontSize = 12.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Spectateurs intéressés Section
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CinemaDarkSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isUserInterested) CinemaGold.copy(alpha = 0.5f) else CinemaDarkBorder),
                        modifier = Modifier.fillMaxWidth().testTag("interested_viewers_section")
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.People,
                                        contentDescription = null,
                                        tint = CinemaGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Spectateurs qui veulent le voir (${interestedMembers.size})",
                                        color = CinemaTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                val audiencePillText = when (movie.targetAudienceType) {
                                    "FAMILY" -> "Toute la famille"
                                    "SOLO" -> "Solo"
                                    else -> "Groupe"
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (movie.targetAudienceType == "FAMILY") CinemaGold else CinemaDarkSurface
                                ) {
                                    Text(
                                        text = audiencePillText,
                                        color = if (movie.targetAudienceType == "FAMILY") Color.Black else CinemaGold,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Member chips / avatars
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                interestedMembers.forEach { mem ->
                                    val isMe = activeMember?.id == mem.id
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = Color(mem.colorHex).copy(alpha = 0.2f),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isMe) CinemaGold else Color(mem.colorHex).copy(alpha = 0.6f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(text = mem.avatarEmoji, fontSize = 14.sp)
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (isMe) "${mem.name} (Moi)" else mem.name,
                                                color = CinemaTextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = if (isMe) FontWeight.Bold else FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            // Join / Leave Button for active user
                            if (activeMember != null) {
                                Spacer(modifier = Modifier.height(12.dp))
                                if (isUserInterested) {
                                    OutlinedButton(
                                        onClick = { onToggleInterested(movie, activeMember) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("leave_movie_interest_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaTextSecondary),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, CinemaDarkBorder)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = CinemaTextSecondary
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Ne plus participer à ce film (se retirer)",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = { onToggleInterested(movie, activeMember) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("join_movie_interest_button"),
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = CinemaGold,
                                            contentColor = Color.Black
                                        )
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp),
                                            tint = Color.Black
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Moi aussi je veux le voir ! (+1)",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Synopsis
                    Text("Synopsis", color = CinemaTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (movie.synopsis.isNotBlank()) movie.synopsis else "Pas de résumé disponible.",
                        color = CinemaTextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = CinemaDarkBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Section Avis & Notations de la famille
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "⭐ Avis & Notes de la Famille",
                                color = CinemaTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${movie.rating} / 5 (${reviews.size} avis)",
                                    color = CinemaGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CinemaGold,
                            modifier = Modifier
                                .clickable { showReviewBottomSheet = true }
                                .testTag("toggle_add_review_button")
                        ) {
                            Text(
                                text = "+ Noter le film",
                                color = Color.Black,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Existing reviews list
                    if (reviews.isEmpty()) {
                        Text(
                            text = "Aucun avis rédigé pour le moment. Soyez le premier à donner votre avis !",
                            color = CinemaTextSecondary,
                            fontSize = 11.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            reviews.forEach { rev ->
                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = CinemaDarkSurfaceVariant),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(text = rev.authorEmoji, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = rev.authorName,
                                                    color = CinemaTextPrimary,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            // Rating stars
                                            Row {
                                                (1..rev.rating).forEach { _ ->
                                                    Icon(Icons.Default.Star, contentDescription = null, tint = StarYellow, modifier = Modifier.size(12.dp))
                                                }
                                            }
                                        }

                                        if (rev.comment.isNotBlank()) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = rev.comment,
                                                color = CinemaTextSecondary,
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onToggleWatched(movie) },
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (movie.isWatched) CinemaDarkSurfaceVariant else CinemaGold
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = if (movie.isWatched) Icons.Outlined.CheckCircleOutline else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (movie.isWatched) CinemaTextPrimary else Color.Black,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (movie.isWatched) "Remettre à voir" else "Marquer comme vu",
                                color = if (movie.isWatched) CinemaTextPrimary else Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }

                        OutlinedButton(
                            onClick = { onDelete(movie) },
                            modifier = Modifier.height(46.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = VelvetRuby),
                            border = androidx.compose.foundation.BorderStroke(1.dp, VelvetRuby.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = VelvetRuby, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }

    if (showReviewBottomSheet) {
        ReviewBottomSheet(
            movie = movie,
            activeMember = activeMember,
            onDismiss = { showReviewBottomSheet = false },
            onSubmitReview = { rating, comment ->
                onAddReview(rating, comment)
            }
        )
    }
}
