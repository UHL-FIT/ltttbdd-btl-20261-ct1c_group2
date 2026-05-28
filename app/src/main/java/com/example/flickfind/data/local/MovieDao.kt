package com.example.flickfind.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {
    @Query("SELECT * FROM watchlist WHERE userId = :userId")
    fun getAllWatchlist(userId: String): Flow<List<MovieEntity>>

    @Query("SELECT * FROM watchlist WHERE userId = :userId")
    fun getAllWatchlistSync(userId: String): List<MovieEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(movie: MovieEntity)

    @Delete
    suspend fun removeFromWatchlist(movie: MovieEntity)

    @Query("DELETE FROM watchlist WHERE id = :movieId AND userId = :userId")
    suspend fun removeFromWatchlistById(movieId: Int, userId: String)

    @Query("UPDATE watchlist SET isWatched = :isWatched WHERE id = :movieId AND userId = :userId")
    suspend fun updateWatchedStatus(movieId: Int, userId: String, isWatched: Boolean)

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE id = :movieId AND userId = :userId)")
    suspend fun isInWatchlist(movieId: Int, userId: String): Boolean

    @Query("SELECT * FROM cached_videos WHERE movieId = :movieId")
    suspend fun getCachedVideos(movieId: Int): CachedVideoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedVideos(entity: CachedVideoEntity)

    // Cache methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCachedMovies(movies: List<CachedMovieEntity>)

    @Query("SELECT * FROM cached_movies WHERE category = :category")
    suspend fun getCachedMovies(category: String): List<CachedMovieEntity>

    @Query("DELETE FROM cached_movies WHERE category = :category")
    suspend fun clearCacheByCategory(category: String)

    @Query("DELETE FROM cached_movies WHERE category LIKE 'search:%' AND timestamp < :threshold")
    suspend fun deleteOldSearchCache(threshold: Long)

    // Search History methods
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSearchQuery(query: SearchHistoryEntity)

    @Query("SELECT * FROM search_history WHERE userId = :userId ORDER BY timestamp DESC LIMIT 10")
    fun getRecentSearchQueries(userId: String): Flow<List<SearchHistoryEntity>>

    @Query("DELETE FROM search_history WHERE `query` = :query AND userId = :userId")
    suspend fun deleteSearchQuery(query: String, userId: String)

    @Query("DELETE FROM search_history WHERE userId = :userId")
    suspend fun clearSearchHistory(userId: String)

    @Query("""
        DELETE FROM search_history 
        WHERE userId = :userId AND `query` NOT IN (
            SELECT `query` FROM search_history 
            WHERE userId = :userId 
            ORDER BY timestamp DESC 
            LIMIT :limit
        )
    """)
    suspend fun trimSearchHistory(userId: String, limit: Int)
}
