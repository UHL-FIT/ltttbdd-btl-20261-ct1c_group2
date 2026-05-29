package com.example.flickfind.data.repository

import android.content.Context
import androidx.work.*
import com.example.flickfind.data.local.CachedVideoEntity
import com.example.flickfind.data.local.CachedMovieEntity
import com.example.flickfind.data.local.MovieDao
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.model.Genre
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.model.Video
import com.example.flickfind.data.model.VideoResponse
import com.example.flickfind.data.remote.TMDBApiService
import com.example.flickfind.worker.MovieNotificationWorker
import com.google.firebase.auth.FirebaseAuth

import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MovieRepository(
    private val apiService: TMDBApiService,
    private val movieDao: MovieDao,
    private val context: Context
) {
    private val apiKey = "75f9cffbe89e18a68c1be474e807b372"
    private val language = "vi-VN"

    private val auth = FirebaseAuth.getInstance()

    suspend fun getNowPlaying(): List<Movie> {
        return try {
            val remoteMovies = apiService.getNowPlaying(apiKey, language).results
            // Cập nhật cache
            movieDao.clearCacheByCategory("now_playing")
            movieDao.insertCachedMovies(remoteMovies.map { it.toCachedEntity("now_playing") })
            remoteMovies
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "Offline Mode: Fetching Now Playing from Cache", e)
            movieDao.getCachedMovies("now_playing").map { it.toMovie() }
        }
    }

    suspend fun getPopular(): List<Movie> {
        return try {
            val remoteMovies = apiService.getPopular(apiKey, language).results
            // Cập nhật cache
            movieDao.clearCacheByCategory("popular")
            movieDao.insertCachedMovies(remoteMovies.map { it.toCachedEntity("popular") })
            remoteMovies
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "Offline Mode: Fetching Popular from Cache", e)
            movieDao.getCachedMovies("popular").map { it.toMovie() }
        }
    }

    private fun Movie.toCachedEntity(category: String): CachedMovieEntity {
        return CachedMovieEntity(
            id = id,
            title = title,
            posterPath = posterPath,
            voteAverage = voteAverage,
            releaseDate = releaseDate,
            overview = overview,
            category = category
        )
    }

    suspend fun searchMovies(query: String): List<Movie> {
        val category = "search:$query"
        val currentTime = System.currentTimeMillis()
        val threshold = currentTime - (24 * 60 * 60 * 1000L) // 24 giờ trước

        // Dọn dẹp cache tìm kiếm cũ để giải phóng bộ nhớ
        try {
            movieDao.deleteOldSearchCache(threshold)
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "Error cleaning old search cache", e)
        }

        return try {
            val remoteMovies = apiService.searchMovies(apiKey, query, language).results
            // Cập nhật cache cho kết quả tìm kiếm
            movieDao.clearCacheByCategory(category)
            movieDao.insertCachedMovies(remoteMovies.map { it.toCachedEntity(category) })
            remoteMovies
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "Offline Mode: Fetching Search Results for '$query' from Cache", e)
            movieDao.getCachedMovies(category).map { it.toMovie() }
        }
    }

    suspend fun getGenres(): List<Genre> {
        return apiService.getGenres(apiKey, language).genres
    }

    suspend fun getMovieDetails(movieId: Int, lang: String = language): Movie {
        return apiService.getMovieDetails(movieId, apiKey, lang)
    }

    suspend fun getMovieVideos(movieId: Int): List<Video> {
        val currentTime = System.currentTimeMillis()
        val ttl = 24 * 60 * 60 * 1000L // 24 hours in ms
        val gson = Gson()

        // 1. Check cache
        val cached = movieDao.getCachedVideos(movieId)
        if (cached != null && (currentTime - cached.timestamp) < ttl) {
            try {
                val response = gson.fromJson(cached.videosJson, VideoResponse::class.java)
                return response.results.filter { it.isYouTube && it.isTrailerOrTeaser }
            } catch (e: Exception) {
                // Parse error, proceed to fetch
            }
        }

        // 2. Fetch from API if offline or cache expired/invalid
        try {
            val response = apiService.getMovieVideos(movieId, apiKey, "en-US")
            val json = gson.toJson(response)
            movieDao.insertCachedVideos(CachedVideoEntity(movieId, json, currentTime))
            return response.results.filter { it.isYouTube && it.isTrailerOrTeaser }
        } catch (e: Exception) {
            // 3. If offline and API fails, return cached if exists (even if expired)
            if (cached != null) {
                try {
                    val response = gson.fromJson(cached.videosJson, VideoResponse::class.java)
                    return response.results.filter { it.isYouTube && it.isTrailerOrTeaser }
                } catch (e2: Exception) {
                    throw e
                }
            }
            throw e
        }
    }

    fun getWatchlist(userId: String): Flow<List<MovieEntity>> = movieDao.getAllWatchlist(userId)

    suspend fun addToWatchlist(movie: Movie) {
        val currentUser = auth.currentUser ?: return
        val entity = MovieEntity(
            id = movie.id,
            userId = currentUser.uid,
            title = movie.title,
            posterPath = movie.posterPath,
            voteAverage = movie.voteAverage,
            releaseDate = movie.releaseDate,
            overview = movie.overview
        )
        movieDao.addToWatchlist(entity)

        scheduleReleaseNotification(movie)
    }



    private fun scheduleReleaseNotification(movie: Movie) {
        val releaseDateStr = movie.releaseDate ?: return
        if (releaseDateStr.isBlank()) return

        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val releaseDate = sdf.parse(releaseDateStr) ?: return
            val currentTime = System.currentTimeMillis()

            // Nếu ngày công chiếu trong tương lai hoặc là hôm nay (trong vòng 24h tới)
            if (releaseDate.time > currentTime - 86400000) { 
                val delay = Math.max(0L, releaseDate.time - currentTime)
                val data = Data.Builder()
                    .putString("movie_title", movie.title)
                    .putInt("movie_id", movie.id)
                    .build()

                val notificationRequest = OneTimeWorkRequestBuilder<MovieNotificationWorker>()
                    .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                    .setInputData(data)
                    .addTag("movie_${movie.id}")
                    .build()

                WorkManager.getInstance(context).enqueueUniqueWork(
                    "notification_${movie.id}",
                    ExistingWorkPolicy.REPLACE,
                    notificationRequest
                )
                android.util.Log.d("MovieRepository", "Scheduled notification for ${movie.title} with delay $delay ms")
            }
        } catch (e: Exception) {
            android.util.Log.e("MovieRepository", "Notification Error: ${e.message}")
        }
    }

    suspend fun removeFromWatchlist(movie: MovieEntity) {
        movieDao.removeFromWatchlist(movie)
    }

    suspend fun updateWatchedStatus(movieId: Int, isWatched: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        movieDao.updateWatchedStatus(movieId, userId, isWatched)
    }



    private fun MovieEntity.toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            posterPath = posterPath,
            voteAverage = voteAverage,
            releaseDate = releaseDate,
            overview = overview
        )
    }

    suspend fun isInWatchlist(movieId: Int): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return movieDao.isInWatchlist(movieId, userId)
    }

    // Search History methods
    fun getSearchHistory(userId: String): Flow<List<com.example.flickfind.data.local.SearchHistoryEntity>> = 
        movieDao.getRecentSearchQueries(userId)

    suspend fun addSearchQuery(query: String) {
        val userId = auth.currentUser?.uid ?: "guest"
        if (query.isNotBlank()) {
            movieDao.insertSearchQuery(com.example.flickfind.data.local.SearchHistoryEntity(query, userId))
            // Chỉ giữ lại 20 mục lịch sử gần nhất cho mỗi user
            movieDao.trimSearchHistory(userId, 20)
        }
    }

    suspend fun deleteSearchQuery(query: String) {
        val userId = auth.currentUser?.uid ?: "guest"
        movieDao.deleteSearchQuery(query, userId)
    }

    suspend fun clearSearchHistory() {
        val userId = auth.currentUser?.uid ?: "guest"
        movieDao.clearSearchHistory(userId)
    }
}
