package com.example.flickfind.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_videos")
data class CachedVideoEntity(
    @PrimaryKey val movieId: Int,
    val videosJson: String,
    val timestamp: Long
)
