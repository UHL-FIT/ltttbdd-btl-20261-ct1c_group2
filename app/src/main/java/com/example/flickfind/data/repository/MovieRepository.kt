package com.example.flickfind.data.repository

import com.example.flickfind.data.local.CachedVideoEntity
import com.example.flickfind.data.local.MovieDao
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.model.Genre
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.model.Video
import com.example.flickfind.data.model.VideoResponse
import com.example.flickfind.data.remote.TMDBApiService
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import android.util.Log

class MovieRepository(
    private val apiService: TMDBApiService,
    private val movieDao: MovieDao,
) {
    // TODO: Thay "YOUR_TMDB_API_KEY" bằng API key thật của bạn từ https://www.themoviedb.org/settings/api
    private val apiKey = "75f9cffbe89e18a68c1be474e807b372"
    private val language = "vi-VN"

    suspend fun getNowPlaying(): List<Movie> {
        return apiService.getNowPlaying(apiKey, language).results
    }

    suspend fun getPopular(): List<Movie> {
        return apiService.getPopular(apiKey, language).results
        val startTime = System.currentTimeMillis()
        Log.d("PERF_TEST", "START_API_GET_POPULAR: $startTime") // Log thời điểm bắt đầu request

        // Giả định apiService.getPopular trả về một đối tượng có thuộc tính 'results' là List<Movie>
        val movies = apiService.getPopular(apiKey, language).results

        val endTime = System.currentTimeMillis()
        val duration = endTime - startTime
        Log.d("PERF_TEST", "END_API_GET_POPULAR: $endTime")   // Log thời điểm kết thúc request
        Log.d("PERF_TEST", "LOAD_TIME_POPULAR: ${duration}ms") // Log tổng thời gian

        return movies
    }

    suspend fun searchMovies(query: String): List<Movie> {
        return apiService.searchMovies(apiKey, query, language).results
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

    val watchlist: Flow<List<MovieEntity>> = movieDao.getAllWatchlist()

    suspend fun addToWatchlist(movie: Movie) {
        movieDao.addToWatchlist(
            MovieEntity(
                id = movie.id,
                title = movie.title,
                posterPath = movie.posterPath,
                voteAverage = movie.voteAverage,
                releaseDate = movie.releaseDate,
                overview = movie.overview
            )
        )
    }

    suspend fun removeFromWatchlist(movie: MovieEntity) {
        movieDao.removeFromWatchlist(movie)
    }

    suspend fun isInWatchlist(movieId: Int): Boolean {
        return movieDao.isInWatchlist(movieId)
    }
}
