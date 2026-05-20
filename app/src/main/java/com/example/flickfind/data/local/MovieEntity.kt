package com.example.flickfind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.flickfind.data.model.Movie

@Entity(tableName = "watchlist")
data class MovieEntity(
    @PrimaryKey val id: Int,
    val title: String,
    val posterPath: String?,
    val voteAverage: Double?,
    val releaseDate: String?,
    val overview: String?
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
