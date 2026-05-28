package com.example.flickfind.data.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("release_date") val releaseDate: String?,
    val overview: String?,
    @SerializedName("genre_ids") val genreIds: List<Int>? = null,
    @SerializedName("production_companies") val productionCompanies: List<ProductionCompany>? = null,
    val credits: CreditsResponse? = null,
    val reviews: ReviewResponse? = null
) {
    val fullPosterUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w500$it" }

    val fullBackdropUrl: String?
        get() = posterPath?.let { "https://image.tmdb.org/t/p/w780$it" }
}

data class ProductionCompany(
    val id: Int,
    val name: String,
    @SerializedName("logo_path") val logoPath: String?
) {
    val fullLogoUrl: String?
        get() = logoPath?.let { "https://image.tmdb.org/t/p/w200$it" }
}

data class CreditsResponse(
    val cast: List<Cast>
)

data class Cast(
    val id: Int,
    val name: String,
    @SerializedName("profile_path") val profilePath: String?,
    val character: String
) {
    val fullProfileUrl: String?
        get() = profilePath?.let { "https://image.tmdb.org/t/p/w185$it" }
}

data class Genre(
    val id: Int,
    val name: String
)

data class GenreResponse(
    val genres: List<Genre>
)

data class MovieResponse(
    val results: List<Movie>,
    val page: Int = 1,
    @SerializedName("total_pages") val totalPages: Int = 1
)

data class Video(
    val id: String,
    @SerializedName("iso_639_1") val language: String?,
    val key: String,
    val name: String,
    val site: String,
    val type: String,
    val official: Boolean
) {
    val isYouTube: Boolean
        get() = site.equals("YouTube", ignoreCase = true)
        
    val isTrailerOrTeaser: Boolean
        get() = type.equals("Trailer", ignoreCase = true) || type.equals("Teaser", ignoreCase = true)
        
    val thumbnailUrl: String
        get() = "https://img.youtube.com/vi/$key/hqdefault.jpg"
}

data class VideoResponse(
    val id: Int,
    val results: List<Video>
)
