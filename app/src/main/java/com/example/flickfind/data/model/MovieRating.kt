package com.example.flickfind.data.model

data class MovieRating(
    val movieId: String = "",
    val userId: String = "",
    val rating: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
