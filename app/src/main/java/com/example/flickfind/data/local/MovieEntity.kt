package com.example.flickfind.data.local

import androidx.room.Entity
import com.example.flickfind.data.model.Movie

@Entity(tableName = "watchlist", primaryKeys = ["id", "userId"])
data class MovieEntity(
    val id: Int = 0,
    val userId: String = "",
    val title: String = "",
    val posterPath: String? = null,
    val voteAverage: Double? = 0.0,
    val releaseDate: String? = "",
    val overview: String? = "",
    val isWatched: Boolean = false
) {
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            posterPath = posterPath,
            voteAverage = voteAverage,
            releaseDate = releaseDate,
            overview = overview
        )
    }
}
