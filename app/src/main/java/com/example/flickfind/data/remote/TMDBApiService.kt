package com.example.flickfind.data.remote

import com.example.flickfind.data.model.GenreResponse
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.model.MovieResponse
import com.example.flickfind.data.model.VideoResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TMDBApiService {

    @GET("movie/now_playing")
    suspend fun getNowPlaying(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/popular")
    suspend fun getPopular(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("api_key") apiKey: String,
        @Query("query") query: String,
        @Query("language") language: String = "vi-VN"
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN",
        @Query("append_to_response") appendToResponse: String = "credits"
    ): Movie

    @GET("movie/{movie_id}/videos")
    suspend fun getMovieVideos(
        @Path("movie_id") movieId: Int,
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "en-US" // Thường trailer TMDB có tiếng Anh đầy đủ hơn
    ): VideoResponse

    @GET("genre/movie/list")
    suspend fun getGenres(
        @Query("api_key") apiKey: String,
        @Query("language") language: String = "vi-VN"
    ): GenreResponse

    companion object {
        const val BASE_URL = "https://api.themoviedb.org/3/"
    }
}
