package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM movies ORDER BY addedTimestamp DESC")
    fun getAllMovies(): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE id = :id LIMIT 1")
    suspend fun getMovieById(id: String): Movie?

    @Query("SELECT * FROM movies WHERE addedByMemberId = :memberId ORDER BY addedTimestamp DESC")
    fun getMoviesByMember(memberId: String): Flow<List<Movie>>

    @Query("SELECT * FROM movies WHERE isWatched = 0 ORDER BY addedTimestamp DESC")
    fun getToWatchMovies(): Flow<List<Movie>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovie(movie: Movie)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMovies(movies: List<Movie>)

    @Update
    suspend fun updateMovie(movie: Movie)

    @Delete
    suspend fun deleteMovie(movie: Movie)

    @Query("SELECT * FROM movies ORDER BY addedTimestamp DESC")
    suspend fun getAllMoviesList(): List<Movie>

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieById(id: String)

    @Query("UPDATE movies SET isWatched = :isWatched WHERE id = :id")
    suspend fun setWatched(id: String, isWatched: Boolean)

    @Query("UPDATE movies SET familyVotes = familyVotes + 1 WHERE id = :id")
    suspend fun upvoteMovie(id: String)
}
