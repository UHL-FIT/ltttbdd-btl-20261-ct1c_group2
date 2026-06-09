package com.example.flickfind.data.model

data class MovieComment(
    val id: String = "",
    val movieId: String = "",
    val userId: String = "",
    val username: String = "",
    val avatar: String? = null,
    val content: String = "",
    val parentId: String? = null, // null if it's a top-level comment, parentCommentId if it's a reply
    val likes: Int = 0,
    val likedBy: List<String> = emptyList(), // Store userIds who liked this comment
    val replyCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
