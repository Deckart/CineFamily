package com.example.data.remote

import java.util.Locale

object StreamingAvailabilityService {

    // Common streaming mapping
    fun detectStreamingProviders(title: String, genres: String, year: Int): List<String> {
        val lower = title.lowercase(Locale.ROOT)
        val services = mutableListOf<String>()

        // Netflix library heuristics
        val netflixKeywords = listOf("spider-man", "retour vers le futur", "stranger", "squid", "avatar", "inception", "interstellar", "shrek", "harry potter", "jurassic", "clara", "matilda")
        if (netflixKeywords.any { lower.contains(it) } || year in 2016..2023) {
            services.add("Netflix")
        }

        // Prime Video library heuristics
        val primeKeywords = listOf("seigneur des anneaux", "interstellar", "coco", "batman", "james bond", "top gun", "gladiator", "paddington", "roi lion", "dune")
        if (primeKeywords.any { lower.contains(it) } || year < 2015) {
            services.add("Amazon Prime Video")
        }

        // Free streaming (France.tv, Arte, Pluto TV, YouTube gratuit)
        val freeKeywords = listOf("amélie poulain", "chihiro", "roi lion", "dragons", "château", "mon voisin totoro", "classique", "charlie chaplin", "molière", "la belle et la bête")
        if (freeKeywords.any { lower.contains(it) } || genres.contains("Animation") || year < 2005) {
            services.add("Gratuit")
        }

        // Default: at least one major service
        if (services.isEmpty()) {
            if (year % 2 == 0) services.add("Netflix") else services.add("Amazon Prime Video")
        }

        return services.distinct()
    }
}
