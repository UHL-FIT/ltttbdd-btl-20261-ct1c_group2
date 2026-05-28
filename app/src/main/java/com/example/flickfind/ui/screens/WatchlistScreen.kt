package com.example.flickfind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.model.Movie
import com.example.flickfind.ui.viewmodel.MovieViewModel
import com.google.firebase.auth.FirebaseAuth

enum class SortOption(val title: String) {
    DEFAULT("Mới thêm"),
    RATING("Điểm cao"),
    RELEASE_DATE("Ngày ra mắt"),
    TITLE("Tên A-Z")
}

@Composable
fun WatchlistScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    onLoginClick: () -> Unit
) {
    val watchlist by viewModel.watchlist.collectAsState()
    val currentUser = FirebaseAuth.getInstance().currentUser

    if (currentUser == null) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.FavoriteBorder,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Mục yêu thích",
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = "Đăng nhập để xem và quản lý danh sách phim yêu thích của bạn.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onLoginClick) {
                Text("Đăng nhập ngay")
            }
        }
    } else {
        WatchlistContent(
            watchlist = watchlist,
            onMovieClick = onMovieClick,
            onWatchlistToggle = { viewModel.toggleWatchlist(it) },
            onWatchedStatusToggle = { movieId, isWatched -> 
                viewModel.updateWatchedStatus(movieId, isWatched)
            }
        )
    }
}

@Composable
fun WatchlistContent(
    watchlist: List<MovieEntity>,
    onMovieClick: (Movie) -> Unit,
    onWatchlistToggle: (Movie) -> Unit,
    onWatchedStatusToggle: (Int, Boolean) -> Unit
) {
    if (watchlist.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Danh sách yêu thích đang trống", style = MaterialTheme.typography.bodyLarge)
            }
        }
    } else {
        var showSuggestDialog by remember { mutableStateOf(false) }
        var currentSortOption by remember { mutableStateOf(SortOption.DEFAULT) }
        
        val sortedWatchlist = remember(watchlist, currentSortOption) {
            when (currentSortOption) {
                SortOption.DEFAULT -> watchlist.reversed() // Mới nhất lên đầu
                SortOption.RATING -> watchlist.sortedByDescending { it.voteAverage ?: 0.0 }
                SortOption.RELEASE_DATE -> watchlist.sortedByDescending { it.releaseDate ?: "" }
                SortOption.TITLE -> watchlist.sortedBy { it.title }
            }
        }

        val randomMovie = remember(watchlist) { watchlist.randomOrNull()?.toMovie() }

        Column(modifier = Modifier.fillMaxSize()) {
            WatchlistSummary(
                watchlist = watchlist,
                onSuggestClick = { showSuggestDialog = true }
            )
            
            // Sắp xếp Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SortOption.values().forEach { option ->
                    FilterChip(
                        selected = currentSortOption == option,
                        onClick = { currentSortOption = option },
                        label = { Text(option.title, style = MaterialTheme.typography.labelSmall) },
                        leadingIcon = if (currentSortOption == option) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(12.dp)) }
                        } else null
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(sortedWatchlist) { entity ->
                    val movie = entity.toMovie()
                    MovieCard(
                        movie = movie,
                        isFavorite = true,
                        isWatched = entity.isWatched,
                        onMovieClick = onMovieClick,
                        onWatchlistClick = onWatchlistToggle,
                        onWatchedClick = { isWatched ->
                            onWatchedStatusToggle(movie.id, isWatched)
                        }
                    )
                }
            }
        }

        if (showSuggestDialog && randomMovie != null) {
            AlertDialog(
                onDismissRequest = { showSuggestDialog = false },
                title = { Text("Gợi ý cho bạn") },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Hôm nay hãy xem thử bộ phim này nhé:")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = randomMovie.title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        showSuggestDialog = false
                        onMovieClick(randomMovie)
                    }) {
                        Text("Xem chi tiết")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSuggestDialog = false }) {
                        Text("Đổi phim khác")
                    }
                }
            )
        }
    }
}

@Composable
fun WatchlistSummary(
    watchlist: List<MovieEntity>,
    onSuggestClick: () -> Unit
) {
    val totalMovies = watchlist.size
    val watchedMovies = watchlist.count { it.isWatched }
    val avgRating = if (watchlist.isNotEmpty()) {
        watchlist.mapNotNull { it.voteAverage }.average()
    } else 0.0
    val topMovie = watchlist.maxByOrNull { it.voteAverage ?: 0.0 }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Bộ sưu tập của bạn",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                IconButton(onClick = onSuggestClick) {
                    Icon(
                        imageVector = Icons.Default.Casino,
                        contentDescription = "Gợi ý ngẫu nhiên",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem(
                    label = "Tổng số phim",
                    value = totalMovies.toString(),
                    icon = Icons.Default.Movie
                )
                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                SummaryItem(
                    label = "Đã xem",
                    value = "$watchedMovies/$totalMovies",
                    icon = Icons.Default.CheckCircle
                )
                VerticalDivider(
                    modifier = Modifier.height(32.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                )
                SummaryItem(
                    label = "Đánh giá TB",
                    value = "%.1f".format(avgRating),
                    icon = Icons.Default.Star
                )
            }
            
            if (topMovie != null) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 12.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Đỉnh nhất: ",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        text = topMovie.title,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun WatchlistScreenPreview() {
    val mockWatchlist = listOf(
        MovieEntity(1, "user123", "Phim ưa thích 1", null, 8.5, "2024-01-01", "Mô tả phim 1"),
        MovieEntity(2, "user123", "Phim ưa thích 2", null, 7.2, "2024-01-02", "Mô tả phim 2")
    )
    WatchlistContent(
        watchlist = mockWatchlist,
        onMovieClick = {},
        onWatchlistToggle = {},
        onWatchedStatusToggle = { _, _ -> }
    )
}
