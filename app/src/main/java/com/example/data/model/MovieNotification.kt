package com.example.data.model

data class MovieNotification(
    val id: String = "",
    val movieId: String = "",
    val movieTitle: String = "",
    val moviePosterUrl: String = "",
    val addedByMemberId: String = "",
    val addedByMemberName: String = "",
    val addedByMemberEmoji: String = "🎬",
    val targetAudienceType: String = "FAMILY",
    val timestamp: Long = System.currentTimeMillis(),
    val interestedMemberIds: List<String> = emptyList()
)
