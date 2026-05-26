package com.example.flickfind.data

import com.example.flickfind.data.local.CachedVideoEntity
import com.example.flickfind.data.local.MovieDao
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.model.GenreResponse
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.model.MovieResponse
import com.example.flickfind.data.model.Video
import com.example.flickfind.data.model.VideoResponse
import com.example.flickfind.data.remote.TMDBApiService
import com.example.flickfind.data.repository.MovieRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class VideoCacheTest {

    class FakeMovieDao : MovieDao {
        var cachedVideo: CachedVideoEntity? = null
        var insertCount = 0

        override fun getAllWatchlist(): Flow<List<MovieEntity>> = emptyFlow()
        override suspend fun addToWatchlist(movie: MovieEntity) {}
        override suspend fun removeFromWatchlist(movie: MovieEntity) {}
        override suspend fun isInWatchlist(movieId: Int): Boolean = false

        override suspend fun getCachedVideos(movieId: Int): CachedVideoEntity? {
            return cachedVideo
        }

        override suspend fun insertCachedVideos(entity: CachedVideoEntity) {
            cachedVideo = entity
            insertCount++
        }
    }

    class FakeApiService : TMDBApiService {
        var fetchCount = 0

        override suspend fun getNowPlaying(apiKey: String, language: String, page: Int): MovieResponse = TODO()
        override suspend fun getPopular(apiKey: String, language: String, page: Int): MovieResponse = TODO()
        override suspend fun searchMovies(apiKey: String, query: String, language: String): MovieResponse = TODO()
        override suspend fun getMovieDetails(movieId: Int, apiKey: String, language: String, appendToResponse: String): Movie = TODO()
        override suspend fun getGenres(apiKey: String, language: String): GenreResponse = TODO()

        override suspend fun getMovieVideos(movieId: Int, apiKey: String, language: String): VideoResponse {
            fetchCount++
            return VideoResponse(
                id = movieId,
                results = listOf(
                    Video("1", "en", "key1", "Trailer 1", "YouTube", "Trailer", true)
                )
            )
        }
    }

    @Test
    fun `getMovieVideos uses cache if valid`() = runBlocking {
        val fakeDao = FakeMovieDao()
        val fakeApi = FakeApiService()
        val repository = MovieRepository(fakeApi, fakeDao)

        // Setup valid cache (1 hour old)
        val json = Gson().toJson(
            VideoResponse(
                1, listOf(Video("2", "en", "key2", "Trailer Cached", "YouTube", "Trailer", true))
            )
        )
        fakeDao.cachedVideo = CachedVideoEntity(1, json, System.currentTimeMillis() - 3600000)

        val result = repository.getMovieVideos(1)
        
        // Assert it uses cache, not api
        assertEquals(0, fakeApi.fetchCount)
        assertEquals(1, result.size)
        assertEquals("key2", result[0].key)
    }

    @Test
    fun `getMovieVideos fetches from API if cache expired`() = runBlocking {
        val fakeDao = FakeMovieDao()
        val fakeApi = FakeApiService()
        val repository = MovieRepository(fakeApi, fakeDao)

        // Setup expired cache (25 hours old)
        val json = Gson().toJson(
            VideoResponse(
                1, listOf(Video("2", "en", "key2", "Trailer Cached", "YouTube", "Trailer", true))
            )
        )
        fakeDao.cachedVideo = CachedVideoEntity(1, json, System.currentTimeMillis() - 25 * 3600000L)

        val result = repository.getMovieVideos(1)
        
        // Assert it fetches from api
        assertEquals(1, fakeApi.fetchCount)
        assertEquals(1, result.size)
        assertEquals("key1", result[0].key) // Fetched from api
        assertEquals(1, fakeDao.insertCount) // Cache updated
    }
}
