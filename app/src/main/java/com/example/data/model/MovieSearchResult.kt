package com.example.data.model

data class MovieSearchResult(
    val title: String,
    val originalTitle: String = "",
    val posterUrl: String = "",
    val year: Int = 2024,
    val durationMinutes: Int = 100,
    val genre: String = "Famille",
    val synopsis: String = "",
    val director: String = "",
    val contentRating: String = "Tous publics",
    val streamingServices: List<String> = listOf("Netflix")
)
