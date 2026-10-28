package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "movies")
data class Movie(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val originalTitle: String = "",
    val posterUrl: String = "",
    val year: Int = 2024,
    val durationMinutes: Int = 105,
    val genres: String = "Famille",
    val synopsis: String = "",
    val director: String = "",
    val rating: Float = 4.2f,
    val contentRating: String = "Tous publics",
    val addedByMemberId: String = "",
    val addedByMemberName: String = "Famille",
    val addedByMemberEmoji: String = "🎬",
    val addedTimestamp: Long = System.currentTimeMillis(),
    val isWatched: Boolean = false,
    val userNotes: String = "",
    val familyVotes: Int = 1,
    // Audience & Security
    val targetAudienceType: String = "FAMILY", // "FAMILY", "SOLO", "GROUP"
    val allowedMemberIds: String = "all", // e.g. "all", or "clara,sophie" (C+S)
    // Streaming providers
    val streamingServices: String = "Netflix", // e.g. "Netflix, Amazon Prime Video, Gratuit"
    // User Reviews summary & details
    val reviewsJson: String = ""
)
