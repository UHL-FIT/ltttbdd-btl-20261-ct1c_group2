package com.example.flickfind.data.repository

import android.util.Log
import com.example.flickfind.data.model.MovieComment
import com.example.flickfind.data.model.MovieRating
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class ReviewRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- RATING OPERATIONS ---

    suspend fun submitRating(movieId: String, rating: Int) {
        val userId = auth.currentUser?.uid ?: return
        val docRef = firestore.collection("movie_ratings").document("${movieId}_${userId}")
        val ratingEntry = MovieRating(
            movieId = movieId,
            userId = userId,
            rating = rating,
            createdAt = System.currentTimeMillis()
        )
        docRef.set(ratingEntry).await()
    }

    fun getUserRating(movieId: String, userId: String): Flow<Int?> = callbackFlow {
        val docRef = firestore.collection("movie_ratings").document("${movieId}_${userId}")
        val listener = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.e("ReviewRepository", "getUserRating error", error)
                return@addSnapshotListener
            }
            if (snapshot != null && snapshot.exists()) {
                val rating = snapshot.toObject(MovieRating::class.java)?.rating
                trySend(rating)
            } else {
                trySend(null)
            }
        }
        awaitClose { listener.remove() }
    }

    fun getRatingStats(movieId: String): Flow<Pair<Double, Map<Int, Int>>> = callbackFlow {
        val listener = firestore.collection("movie_ratings")
            .whereEqualTo("movieId", movieId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ReviewRepository", "getRatingStats error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val ratings = snapshot.documents.mapNotNull { it.toObject(MovieRating::class.java) }
                    val total = ratings.size
                    if (total == 0) {
                        trySend(0.0 to emptyMap())
                        return@addSnapshotListener
                    }
                    val sum = ratings.sumOf { it.rating }
                    val avg = sum.toDouble() / total
                    val breakdown = ratings.groupBy { it.rating }.mapValues { it.value.size }
                    trySend(avg to breakdown)
                }
            }
        awaitClose { listener.remove() }
    }

    // --- COMMENT OPERATIONS ---

    fun getComments(movieId: String): Flow<List<MovieComment>> = callbackFlow {
        val listener = firestore.collection("movie_comments")
            .whereEqualTo("movieId", movieId)
            .whereEqualTo("parentId", null)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ReviewRepository", "getComments error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MovieComment::class.java)?.copy(id = doc.id)
                    }
                    trySend(comments)
                }
            }
        awaitClose { listener.remove() }
    }

    fun getReplies(parentId: String): Flow<List<MovieComment>> = callbackFlow {
        val listener = firestore.collection("movie_comments")
            .whereEqualTo("parentId", parentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ReviewRepository", "getReplies error", error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val replies = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(MovieComment::class.java)?.copy(id = doc.id)
                    }
                    trySend(replies)
                }
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(movieId: String, content: String) {
        val currentUser = auth.currentUser ?: return
        val username = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Người dùng"
        val avatar = currentUser.photoUrl?.toString()

        val commentRef = firestore.collection("movie_comments").document()
        val newComment = MovieComment(
            id = commentRef.id,
            movieId = movieId,
            userId = currentUser.uid,
            username = username,
            avatar = avatar,
            content = content,
            parentId = null,
            likes = 0,
            likedBy = emptyList(),
            replyCount = 0,
            createdAt = System.currentTimeMillis()
        )
        commentRef.set(newComment).await()
    }

    suspend fun addReply(movieId: String, parentId: String, content: String) {
        val currentUser = auth.currentUser ?: return
        val username = currentUser.displayName ?: currentUser.email?.substringBefore("@") ?: "Người dùng"
        val avatar = currentUser.photoUrl?.toString()

        val replyRef = firestore.collection("movie_comments").document()
        val parentRef = firestore.collection("movie_comments").document(parentId)

        val newReply = MovieComment(
            id = replyRef.id,
            movieId = movieId,
            userId = currentUser.uid,
            username = username,
            avatar = avatar,
            content = content,
            parentId = parentId,
            likes = 0,
            likedBy = emptyList(),
            replyCount = 0,
            createdAt = System.currentTimeMillis()
        )

        firestore.runTransaction { transaction ->
            // Thêm reply
            transaction.set(replyRef, newReply)
            // Tăng số lượng reply của comment cha
            transaction.update(parentRef, "replyCount", FieldValue.increment(1))
        }.await()
    }

    suspend fun deleteComment(commentId: String, parentId: String? = null) {
        val commentRef = firestore.collection("movie_comments").document(commentId)
        
        firestore.runTransaction { transaction ->
            transaction.delete(commentRef)
            if (parentId != null) {
                val parentRef = firestore.collection("movie_comments").document(parentId)
                transaction.update(parentRef, "replyCount", FieldValue.increment(-1))
            }
        }.await()
    }

    suspend fun toggleLikeComment(commentId: String, userId: String) {
        val commentRef = firestore.collection("movie_comments").document(commentId)

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(commentRef)
            val likedBy = snapshot.get("likedBy") as? List<*> ?: emptyList<String>()
            val isLiked = likedBy.contains(userId)

            if (isLiked) {
                transaction.update(commentRef, "likedBy", FieldValue.arrayRemove(userId))
                transaction.update(commentRef, "likes", FieldValue.increment(-1))
            } else {
                transaction.update(commentRef, "likedBy", FieldValue.arrayUnion(userId))
                transaction.update(commentRef, "likes", FieldValue.increment(1))
            }
        }.await()
    }
}
