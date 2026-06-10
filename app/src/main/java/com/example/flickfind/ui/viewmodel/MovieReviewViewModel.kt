package com.example.flickfind.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.flickfind.data.model.MovieComment
import com.example.flickfind.data.repository.ReviewRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MovieReviewViewModel(private val repository: ReviewRepository) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _movieId = MutableStateFlow<String?>(null)
    val movieId: StateFlow<String?> = _movieId.asStateFlow()

    private val _sortBy = MutableStateFlow("NEWEST") // "NEWEST", "LIKES"
    val sortBy: StateFlow<String> = _sortBy.asStateFlow()

    // Top-level comments flow
    private val _rawComments = MutableStateFlow<List<MovieComment>>(emptyList())
    
    val comments: StateFlow<List<MovieComment>> = combine(_rawComments, _sortBy) { list, sort ->
        when (sort) {
            "LIKES" -> list.sortedWith(compareByDescending<MovieComment> { it.likes }.thenByDescending { it.createdAt })
            else -> list.sortedByDescending { it.createdAt }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Map of parentCommentId -> list of reply comments
    private val _replies = MutableStateFlow<Map<String, List<MovieComment>>>(emptyMap())
    val replies: StateFlow<Map<String, List<MovieComment>>> = _replies.asStateFlow()

    // Rating stats: averageRating to starBreakdown (map of star -> count)
    private val _ratingStats = MutableStateFlow<Pair<Double, Map<Int, Int>>>(0.0 to emptyMap())
    val ratingStats: StateFlow<Pair<Double, Map<Int, Int>>> = _ratingStats.asStateFlow()

    // Current user's rating for the selected movie
    private val _userRating = MutableStateFlow<Int?>(null)
    val userRating: StateFlow<Int?> = _userRating.asStateFlow()

    private var activeMovieJobs: List<Job> = emptyList()
    private val replyJobs = mutableMapOf<String, Job>()

    fun setMovieId(id: String) {
        // Always cancel and restart listeners to ensure fresh data
        // This handles: new movie, account switch, or re-navigation
        cancelAllListeners()
        
        _movieId.value = id
        _replies.value = emptyMap()
        _userRating.value = null
        _ratingStats.value = 0.0 to emptyMap()
        _rawComments.value = emptyList()

        if (id.isBlank()) return

        startListeners(id)
    }

    private fun cancelAllListeners() {
        activeMovieJobs.forEach { it.cancel() }
        replyJobs.values.forEach { it.cancel() }
        replyJobs.clear()
    }

    private fun startListeners(id: String) {
        // Start listening to Firestore flows
        val jobStats = viewModelScope.launch {
            repository.getRatingStats(id)
                .catch { e -> e.printStackTrace() }
                .collect {
                    _ratingStats.value = it
                }
        }

        val jobComments = viewModelScope.launch {
            repository.getComments(id)
                .catch { e -> e.printStackTrace() }
                .collect {
                    _rawComments.value = it
                }
        }

        val jobUserRating = viewModelScope.launch {
            val userId = auth.currentUser?.uid
            if (userId != null) {
                repository.getUserRating(id, userId)
                    .catch { e -> e.printStackTrace() }
                    .collect {
                        _userRating.value = it
                    }
            } else {
                _userRating.value = null
            }
        }

        activeMovieJobs = listOf(jobStats, jobComments, jobUserRating)
    }

    // Load/Subscribe to replies for a parent comment
    fun loadReplies(parentId: String) {
        if (replyJobs.containsKey(parentId)) return // Already listening

        val job = viewModelScope.launch {
            repository.getReplies(parentId)
                .catch { e -> e.printStackTrace() }
                .collect { replyList ->
                    val currentReplies = _replies.value.toMutableMap()
                    currentReplies[parentId] = replyList.sortedBy { it.createdAt }
                    _replies.value = currentReplies
                }
        }
        replyJobs[parentId] = job
    }

    fun submitRating(rating: Int) {
        val currentMovieId = _movieId.value ?: return
        viewModelScope.launch {
            try {
                repository.submitRating(currentMovieId, rating)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitComment(content: String) {
        val currentMovieId = _movieId.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addComment(currentMovieId, content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitReply(parentId: String, content: String) {
        val currentMovieId = _movieId.value ?: return
        if (content.isBlank()) return
        viewModelScope.launch {
            try {
                repository.addReply(currentMovieId, parentId, content)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteComment(commentId: String, parentId: String? = null) {
        viewModelScope.launch {
            try {
                repository.deleteComment(commentId, parentId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun toggleLikeComment(commentId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                repository.toggleLikeComment(commentId, userId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setSortOption(sort: String) {
        _sortBy.value = sort
    }

    override fun onCleared() {
        super.onCleared()
        activeMovieJobs.forEach { it.cancel() }
        replyJobs.values.forEach { it.cancel() }
    }
}

class MovieReviewViewModelFactory(private val repository: ReviewRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MovieReviewViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MovieReviewViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
