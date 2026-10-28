package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ItunesSearchResponse(
    @Json(name = "resultCount") val resultCount: Int = 0,
    @Json(name = "results") val results: List<ItunesMovieItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class ItunesMovieItem(
    @Json(name = "trackId") val trackId: Long? = null,
    @Json(name = "trackName") val trackName: String? = null,
    @Json(name = "collectionName") val collectionName: String? = null,
    @Json(name = "artworkUrl100") val artworkUrl100: String? = null,
    @Json(name = "artworkUrl60") val artworkUrl60: String? = null,
    @Json(name = "primaryGenreName") val primaryGenreName: String? = null,
    @Json(name = "trackTimeMillis") val trackTimeMillis: Long? = null,
    @Json(name = "releaseDate") val releaseDate: String? = null,
    @Json(name = "longDescription") val longDescription: String? = null,
    @Json(name = "shortDescription") val shortDescription: String? = null,
    @Json(name = "artistName") val artistName: String? = null,
    @Json(name = "contentAdvisoryRating") val contentAdvisoryRating: String? = null
)
