package com.example.flickfind.data.model

import com.google.gson.annotations.SerializedName

data class ReviewResponse(
    val results: List<Review>
)

data class Review(
    val id: String,
    val author: String,
    val content: String,
    @SerializedName("created_at") val createdAt: String,
    @SerializedName("author_details") val authorDetails: AuthorDetails
)

data class AuthorDetails(
    val rating: Double?
)
