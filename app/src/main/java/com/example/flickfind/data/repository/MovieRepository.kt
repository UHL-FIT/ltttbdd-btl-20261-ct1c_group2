package com.example.flickfind.data.repository

import com.example.flickfind.data.local.MovieDao
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.model.Genre
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.remote.TMDBApiService
import kotlinx.coroutines.flow.Flow

class MovieRepository(
    private val apiService: TMDBApiService,
    private val movieDao: MovieDao
) {
    // TODO: Thay "YOUR_TMDB_API_KEY" bằng API key thật của bạn từ https://www.themoviedb.org/settings/api
    private val apiKey = "75f9cffbe89e18a68c1be474e807b372"
    private val language = "vi-VN"

    suspend fun getNowPlaying(): List<Movie> {
        return apiService.getNowPlaying(apiKey, language).results
    }

    suspend fun getPopular(): List<Movie> {
        return apiService.getPopular(apiKey, language).results
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
