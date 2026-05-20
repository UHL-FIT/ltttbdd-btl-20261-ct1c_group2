package com.example.flickfind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.model.Movie
import com.example.flickfind.ui.viewmodel.MovieViewModel

@Composable
fun WatchlistScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val watchlist by viewModel.watchlist.collectAsState()

    WatchlistContent(
        watchlist = watchlist,
        onMovieClick = onMovieClick,
        onWatchlistClick = { viewModel.toggleWatchlist(it) }
    )
}

@Composable
fun WatchlistContent(
    watchlist: List<MovieEntity>,
    onMovieClick: (Movie) -> Unit,
    onWatchlistClick: (Movie) -> Unit
) {
    if (watchlist.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Danh sách trống")
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(watchlist) { entity ->
                val movie = entity.toMovie()
                MovieCard(
                    movie = movie,
                    isFavorite = true,
                    onMovieClick = onMovieClick,
                    onWatchlistClick = onWatchlistClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WatchlistScreenPreview() {
    val mockWatchlist = listOf(
        MovieEntity(1, "Phim ưa thích 1", null, 8.5, "2024-01-01", "Mô tả phim 1"),
        MovieEntity(2, "Phim ưa thích 2", null, 7.2, "2024-01-02", "Mô tả phim 2")
    )
    WatchlistContent(
        watchlist = mockWatchlist,
        onMovieClick = {},
        onWatchlistClick = {}
    )
}

