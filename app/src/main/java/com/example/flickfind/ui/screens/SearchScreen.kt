package com.example.flickfind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.ui.viewmodel.MovieViewModel

@Composable
fun SearchScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val searchResults by viewModel.searchResults.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val selectedGenreId by viewModel.selectedGenreId.collectAsState()

    // Lọc theo genre ở client
    val baseMovies = if (query.isBlank()) popularMovies else searchResults
    val displayMovies = if (selectedGenreId != null) {
        baseMovies.filter { it.genreIds?.contains(selectedGenreId) == true }
    } else {
        baseMovies
    }
    
    val title = if (query.isBlank()) "Gợi ý phim hot" else "Kết quả tìm kiếm"

    SearchContent(
        query = query,
        onQueryChange = {
            query = it
            viewModel.searchMovies(it)
        },
        displayMovies = displayMovies,
        title = title,
        watchlist = watchlist,
        genres = genres,
        selectedGenreId = selectedGenreId,
        onGenreClick = { viewModel.selectGenre(if (selectedGenreId == it) null else it) },
        onMovieClick = onMovieClick,
        onWatchlistClick = { viewModel.toggleWatchlist(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    query: String,
    onQueryChange: (String) -> Unit,
    displayMovies: List<Movie>,
    title: String,
    watchlist: List<com.example.flickfind.data.local.MovieEntity>,
    genres: List<com.example.flickfind.data.model.Genre>,
    selectedGenreId: Int?,
    onGenreClick: (Int) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onWatchlistClick: (Movie) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Tìm kiếm phim...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            singleLine = true
        )

        if (genres.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(genres) { genre ->
                    FilterChip(
                        selected = selectedGenreId == genre.id,
                        onClick = { onGenreClick(genre.id) },
                        label = { Text(genre.name) }
                    )
                }
            }
        }

        if (displayMovies.isNotEmpty()) {
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(displayMovies) { movie ->
                val isFavorite = watchlist.any { it.id == movie.id }
                MovieCard(
                    movie = movie,
                    isFavorite = isFavorite,
                    onMovieClick = onMovieClick,
                    onWatchlistClick = onWatchlistClick
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val mockMovies = listOf(
        Movie(1, "Phim tìm kiếm 1", null, 8.5, "2024-01-01", "Mô tả phim 1"),
        Movie(2, "Phim tìm kiếm 2", null, 7.2, "2024-01-02", "Mô tả phim 2")
    )
    SearchContent(
        query = "Deadpool",
        onQueryChange = {},
        displayMovies = mockMovies,
        title = "Kết quả tìm kiếm",
        watchlist = emptyList(),
        genres = emptyList(),
        selectedGenreId = null,
        onGenreClick = {},
        onMovieClick = {},
        onWatchlistClick = {}
    )
}
