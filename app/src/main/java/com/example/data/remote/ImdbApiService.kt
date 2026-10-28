package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// OMDb (Open Movie Database for IMDb)
@JsonClass(generateAdapter = true)
data class OmdbSearchResponse(
    @Json(name = "Search") val search: List<OmdbSearchItem>? = null,
    @Json(name = "totalResults") val totalResults: String? = null,
    @Json(name = "Response") val response: String? = null,
    @Json(name = "Error") val error: String? = null
)

@JsonClass(generateAdapter = true)
data class OmdbSearchItem(
    @Json(name = "Title") val title: String? = null,
    @Json(name = "Year") val year: String? = null,
    @Json(name = "imdbID") val imdbId: String? = null,
    @Json(name = "Type") val type: String? = null,
    @Json(name = "Poster") val poster: String? = null
)

@JsonClass(generateAdapter = true)
data class OmdbMovieDetail(
    @Json(name = "Title") val title: String? = null,
    @Json(name = "Year") val year: String? = null,
    @Json(name = "Rated") val rated: String? = null,
    @Json(name = "Released") val released: String? = null,
    @Json(name = "Runtime") val runtime: String? = null,
    @Json(name = "Genre") val genre: String? = null,
    @Json(name = "Director") val director: String? = null,
    @Json(name = "Actors") val actors: String? = null,
    @Json(name = "Plot") val plot: String? = null,
    @Json(name = "Poster") val poster: String? = null,
    @Json(name = "imdbRating") val imdbRating: String? = null,
    @Json(name = "imdbID") val imdbId: String? = null,
    @Json(name = "Response") val response: String? = null
)

// Official IMDb Suggestions
@JsonClass(generateAdapter = true)
data class ImdbSuggestionResponse(
    @Json(name = "d") val results: List<ImdbSuggestionItem>? = null
)

@JsonClass(generateAdapter = true)
data class ImdbSuggestionItem(
    @Json(name = "id") val id: String? = null,
    @Json(name = "l") val title: String? = null,
    @Json(name = "y") val year: Int? = null,
    @Json(name = "s") val stars: String? = null,
    @Json(name = "q") val type: String? = null,
    @Json(name = "i") val image: ImdbImage? = null
)

@JsonClass(generateAdapter = true)
data class ImdbImage(
    @Json(name = "imageUrl") val imageUrl: String? = null
)

interface OmdbApiService {
    @GET("/")
    suspend fun searchMovies(
        @Query("s") query: String,
        @Query("type") type: String = "movie",
        @Query("apikey") apiKey: String = "trilogy"
    ): OmdbSearchResponse

    @GET("/")
    suspend fun getMovieDetailByTitle(
        @Query("t") title: String,
        @Query("apikey") apiKey: String = "trilogy"
    ): OmdbMovieDetail

    @GET("/")
    suspend fun getMovieDetailById(
        @Query("i") imdbId: String,
        @Query("apikey") apiKey: String = "trilogy"
    ): OmdbMovieDetail

    companion object {
        fun create(): OmdbApiService {
            val client = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

            return Retrofit.Builder()
                .baseUrl("https://www.omdbapi.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(OmdbApiService::class.java)
        }
    }
}

interface ImdbSuggestionService {
    @GET("suggestion/{first_char}/{query}.json")
    suspend fun getSuggestions(
        @Path("first_char") firstChar: String,
        @Path("query") query: String
    ): ImdbSuggestionResponse

    companion object {
        fun create(): ImdbSuggestionService {
            val client = OkHttpClient.Builder()
                .connectTimeout(12, TimeUnit.SECONDS)
                .readTimeout(12, TimeUnit.SECONDS)
                .build()

            val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

            return Retrofit.Builder()
                .baseUrl("https://v3.sg.media-imdb.com/")
                .client(client)
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .build()
                .create(ImdbSuggestionService::class.java)
        }
    }
}
