package com.example.flickfind.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.flickfind.data.model.Movie
import com.example.flickfind.ui.viewmodel.MovieViewModel

@Composable
fun HomeScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit
) {
    val nowPlaying by viewModel.nowPlayingMovies.collectAsState()
    val popular by viewModel.popularMovies.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val isLoadingNowPlaying by viewModel.isLoadingNowPlaying.collectAsState()
    val isLoadingPopular by viewModel.isLoadingPopular.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    HomeContent(
        nowPlaying = nowPlaying,
        popular = popular,
        watchlist = watchlist,
        isLoadingNowPlaying = isLoadingNowPlaying,
        isLoadingPopular = isLoadingPopular,
        errorMessage = errorMessage,
        onMovieClick = onMovieClick,
        onWatchlistClick = { viewModel.toggleWatchlist(it) },
        onRefresh = { viewModel.refreshAll() }
    )
}

@Composable
fun HomeContent(
    nowPlaying: List<Movie>,
    popular: List<Movie>,
    watchlist: List<com.example.flickfind.data.local.MovieEntity> = emptyList(),
    isLoadingNowPlaying: Boolean = false,
    isLoadingPopular: Boolean = false,
    errorMessage: String? = null,
    onMovieClick: (Movie) -> Unit,
    onWatchlistClick: (Movie) -> Unit,
    onRefresh: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 16.dp)
    ) {
        // Hiển thị lỗi nếu có
        errorMessage?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = it, color = MaterialTheme.colorScheme.onErrorContainer)
                    Button(onClick = onRefresh, modifier = Modifier.align(Alignment.End)) {
                        Text("Thử lại")
                    }
                }
            }
        }

        // Phần 1: Banners (Top 5 phim phổ biến)
        if (popular.isNotEmpty()) {
            val bannerMovies = popular.take(5)
            val pagerState = rememberPagerState(pageCount = { bannerMovies.size })
            
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) { page ->
                BannerItem(movie = bannerMovies[page])
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        } else if (isLoadingPopular) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        // Phần 2: Danh sách Phim Đang Chiếu
        MovieSection(
            title = "Phim Đang Chiếu",
            movies = nowPlaying,
            watchlist = watchlist,
            isLoading = isLoadingNowPlaying,
            onMovieClick = onMovieClick,
            onWatchlistClick = onWatchlistClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Phần 3: Danh sách Phim Phổ Biến
        MovieSection(
            title = "Phim Phổ Biến",
            movies = popular,
            watchlist = watchlist,
            isLoading = isLoadingPopular,
            onMovieClick = onMovieClick,
            onWatchlistClick = onWatchlistClick
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Nút Refresh để tải lại dữ liệu nếu bị trống
        if (nowPlaying.isEmpty() && popular.isEmpty() && !isLoadingNowPlaying && !isLoadingPopular) {
            Button(
                onClick = { onRefresh() },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Tải lại dữ liệu")
            }
        }
    }
}



@Composable
fun BannerItem(movie: Movie) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = movie.fullPosterUrl,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
            error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
        )
        // Hiệu ứng Gradient mờ dần từ dưới lên
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.9f)),
                        startY = 400f
                    )
                )
        )
        Text(
            text = movie.title,
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp),
            color = Color.White,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun MovieSection(
    title: String,
    movies: List<Movie>,
    watchlist: List<com.example.flickfind.data.local.MovieEntity> = emptyList(),
    isLoading: Boolean = false,
    onMovieClick: (Movie) -> Unit,
    onWatchlistClick: (Movie) -> Unit
) {
    Column {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (isLoading && movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp))
            }
        } else if (movies.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Không có phim nào", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(movies) { movie ->
                    val isFavorite = watchlist.any { it.id == movie.id }
                    Box(modifier = Modifier.width(160.dp)) {
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
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val mockMovies = listOf(
        Movie(1, "Deadpool & Wolverine", null, 8.5, "2024-07-24", "Mô tả phim"),
        Movie(2, "Inside Out 2", null, 7.2, "2024-06-12", "Mô tả phim"),
        Movie(3, "Moana 2", null, 7.5, "2024-11-27", "Mô tả phim")
    )
    HomeContent(
        nowPlaying = mockMovies,
        popular = mockMovies,
        isLoadingNowPlaying = false,
        isLoadingPopular = false,
        onMovieClick = {},
        onWatchlistClick = {}
    )
}
