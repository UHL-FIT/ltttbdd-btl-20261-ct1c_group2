package com.example.flickfind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.local.SearchHistoryEntity
import com.example.flickfind.data.model.Genre
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.model.Video
import com.example.flickfind.data.repository.MovieRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

import kotlinx.coroutines.Job
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MovieViewModel(private val repository: MovieRepository) : ViewModel() {
    private var watchlistJob: Job? = null

    private val _nowPlayingMovies = MutableStateFlow<List<Movie>>(emptyList())
    val nowPlayingMovies: StateFlow<List<Movie>> = _nowPlayingMovies.asStateFlow()

    private val _popularMovies = MutableStateFlow<List<Movie>>(emptyList())
    val popularMovies: StateFlow<List<Movie>> = _popularMovies.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Movie>>(emptyList())
    val searchResults: StateFlow<List<Movie>> = _searchResults.asStateFlow()

    private val _genres = MutableStateFlow<List<Genre>>(emptyList())
    val genres: StateFlow<List<Genre>> = _genres.asStateFlow()

    private val _selectedGenreId = MutableStateFlow<Int?>(null)
    val selectedGenreId: StateFlow<Int?> = _selectedGenreId.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _watchlist = MutableStateFlow<List<MovieEntity>>(emptyList())
    val watchlist: StateFlow<List<MovieEntity>> = _watchlist.asStateFlow()

    private val _searchHistory = MutableStateFlow<List<SearchHistoryEntity>>(emptyList())
    val searchHistory: StateFlow<List<SearchHistoryEntity>> = _searchHistory.asStateFlow()

    private val _isLoadingNowPlaying = MutableStateFlow(false)
    val isLoadingNowPlaying: StateFlow<Boolean> = _isLoadingNowPlaying.asStateFlow()

    private val _isLoadingPopular = MutableStateFlow(false)
    val isLoadingPopular: StateFlow<Boolean> = _isLoadingPopular.asStateFlow()

    private val _selectedMovie = MutableStateFlow<Movie?>(null)
    val selectedMovie: StateFlow<Movie?> = _selectedMovie.asStateFlow()

    private val _movieVideos = MutableStateFlow<List<Video>>(emptyList())
    val movieVideos: StateFlow<List<Video>> = _movieVideos.asStateFlow()

    private val _isLoadingDetail = MutableStateFlow(false)
    val isLoadingDetail: StateFlow<Boolean> = _isLoadingDetail.asStateFlow()

    private val _isLoadingSearch = MutableStateFlow(false)
    val isLoadingSearch: StateFlow<Boolean> = _isLoadingSearch.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // isRefreshing = true khi đang tải nowPlaying hoặc popular
    val isRefreshing: StateFlow<Boolean> = combine(_isLoadingNowPlaying, _isLoadingPopular) { a, b -> a || b }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    init {
        refreshAll()
        fetchGenres()
    }

    fun loadWatchlistForUser(uid: String?) {
        watchlistJob?.cancel()
        if (uid != null) {
            watchlistJob = viewModelScope.launch {

                
                // Thu thập Watchlist
                launch {
                    repository.getWatchlist(uid).collectLatest {
                        _watchlist.value = it
                    }
                }

                // Thu thập Lịch sử tìm kiếm
                launch {
                    repository.getSearchHistory(uid).collectLatest {
                        _searchHistory.value = it
                    }
                }
            }
        } else {
            _watchlist.value = emptyList()
            // Đối với guest, có thể lấy lịch sử mặc định "guest"
            watchlistJob = viewModelScope.launch {
                repository.getSearchHistory("guest").collectLatest {
                    _searchHistory.value = it
                }
            }
        }
    }



    fun refreshAll() {
        fetchNowPlaying()
        fetchPopular()
    }

    private fun fetchGenres() {
        viewModelScope.launch {
            try {
                val results = repository.getGenres()
                _genres.value = results
            } catch (e: Exception) {
                android.util.Log.e("MovieViewModel", "Error fetching genres: ${e.message}", e)
            }
        }
    }

    fun selectGenre(genreId: Int?) {
        _selectedGenreId.value = genreId
    }

    fun fetchNowPlaying() {
        viewModelScope.launch {
            _isLoadingNowPlaying.value = true
            _errorMessage.value = null
            try {
                val results = repository.getNowPlaying()
                _nowPlayingMovies.value = results
            } catch (e: Exception) {
                android.util.Log.e("MovieViewModel", "Error fetching now playing: ${e.message}", e)
                _errorMessage.value = "Lỗi kết nối: ${e.message}"
                _nowPlayingMovies.value = emptyList()
            } finally {
                _isLoadingNowPlaying.value = false
            }
        }
    }

    fun fetchPopular() {
        viewModelScope.launch {
            _isLoadingPopular.value = true
            _errorMessage.value = null
            try {
                val results = repository.getPopular()
                _popularMovies.value = results
            } catch (e: Exception) {
                android.util.Log.e("MovieViewModel", "Error fetching popular: ${e.message}", e)
                _errorMessage.value = "Lỗi kết nối: ${e.message}"
                _popularMovies.value = emptyList()
            } finally {
                _isLoadingPopular.value = false
            }
        }
    }

    private var searchJob: Job? = null

    fun searchMovies(query: String, saveToHistory: Boolean = false) {
        // Nếu query giống hệt query cũ và đã có kết quả, không cần search lại (trừ khi cần lưu history)
        if (_searchQuery.value == query && _searchResults.value.isNotEmpty() && !saveToHistory) return
        
        _searchQuery.value = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            if (query.isBlank()) {
                _searchResults.value = emptyList()
            } else {
                try {
                    _isLoadingSearch.value = true
                    val results = repository.searchMovies(query)
                    _searchResults.value = results
                    if (saveToHistory) {
                        addSearchQuery(query)
                    }
                } catch (e: Exception) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        android.util.Log.e("MovieViewModel", "Error searching: ${e.message}", e)
                        _searchResults.value = emptyList()
                    }
                } finally {
                    _isLoadingSearch.value = false
                }
            }
        }
    }

    fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            repository.addSearchQuery(query.trim())
        }
    }

    fun deleteSearchQuery(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    fun toggleWatchlist(movie: Movie) {
        viewModelScope.launch {
            val isFavorite = watchlist.value.any { it.id == movie.id }
            if (isFavorite) {
                val entity = watchlist.value.find { it.id == movie.id }
                entity?.let { repository.removeFromWatchlist(it) }
            } else {
                repository.addToWatchlist(movie)
            }
        }
    }

    fun updateWatchedStatus(movieId: Int, isWatched: Boolean) {
        viewModelScope.launch {
            repository.updateWatchedStatus(movieId, isWatched)
        }
    }

    fun removeFromWatchlist(movie: MovieEntity) {
        viewModelScope.launch {
            repository.removeFromWatchlist(movie)
        }
    }

    fun fetchMovieDetails(movieId: Int) {
        viewModelScope.launch {
            _isLoadingDetail.value = true
            _errorMessage.value = null
            try {
                val movie = repository.getMovieDetails(movieId)
                if (movie.overview.isNullOrBlank()) {
                    // Nếu không có mô tả tiếng Việt, thử lấy bản tiếng Anh
                    val englishMovie = repository.getMovieDetails(movieId, "en-US")
                    _selectedMovie.value = movie.copy(overview = englishMovie.overview)
                } else {
                    _selectedMovie.value = movie
                }
                
                // Fetch videos after fetching movie details
                try {
                    _movieVideos.value = repository.getMovieVideos(movieId)
                } catch (e: Exception) {
                    android.util.Log.e("MovieViewModel", "Error fetching videos: ${e.message}", e)
                    _movieVideos.value = emptyList()
                }
                
            } catch (e: Exception) {
                android.util.Log.e("MovieViewModel", "Error fetching movie details: ${e.message}", e)
                _errorMessage.value = "Không thể tải chi tiết phim: ${e.message}"
            } finally {
                _isLoadingDetail.value = false
            }
        }
    }

    fun clearSelectedMovie() {
        _selectedMovie.value = null
        _movieVideos.value = emptyList()
    }
}

class MovieViewModelFactory(private val repository: MovieRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
