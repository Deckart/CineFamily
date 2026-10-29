package com.example.data.model

data class MovieNight(
    val id: String = "",
    val movieId: String = "",
    val movieTitle: String = "",
    val moviePosterUrl: String = "",
    val movieDurationMinutes: Int = 100,
    val organizerMemberId: String = "",
    val organizerMemberName: String = "",
    val organizerMemberEmoji: String = "🎬",
    val dateTimeMillis: Long = System.currentTimeMillis() + 86400000L,
    val timeString: String = "20h30",
    val themeOrNotes: String = "",
    val snacksDescription: String = "",
    val invitedMemberIds: List<String> = emptyList(),
    val attendingMemberIds: List<String> = emptyList(),
    val declinedMemberIds: List<String> = emptyList(),
    val createdAt: Long = System.currentTimeMillis()
) {
    val isUpcoming: Boolean
        get() = dateTimeMillis >= System.currentTimeMillis() - 4 * 3600 * 1000L // remains active for 4h after start
}
