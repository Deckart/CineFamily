package com.example.data.model

data class Review(
    val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorEmoji: String = "🎬",
    val rating: Int = 5, // 1 to 5 stars
    val comment: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
