package com.example.flickfind.data.local

import androidx.room.Entity

@Entity(
    tableName = "search_history",
    primaryKeys = ["query", "userId"],
    indices = [androidx.room.Index(value = ["userId", "timestamp"])]
)
data class SearchHistoryEntity(
    val query: String,
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)
