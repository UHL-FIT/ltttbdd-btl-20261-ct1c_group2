package com.example.flickfind.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.flickfind.data.model.Movie
import com.example.flickfind.ui.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterialApi::class)
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
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Trạng thái PullToRefresh từ thư viện Material
    val pullRefreshState = rememberPullRefreshState(
        refreshing = isRefreshing,
        onRefresh = { viewModel.refreshAll() }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pullRefresh(pullRefreshState) // Gắn gesture kéo xuống
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp)
        ) {
            // Hiển thị lỗi nếu có
            errorMessage?.let { msg ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = msg, color = MaterialTheme.colorScheme.onErrorContainer)
                        Button(
                            onClick = { viewModel.refreshAll() },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Thử lại")
                        }
                    }
                }
            }

            // Phần Banner (Top 5 phim phổ biến)
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

                // Indicator chấm trang
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(bannerMovies.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 3.dp)
                                .size(if (isSelected) 10.dp else 6.dp)
                                .background(
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                    shape = androidx.compose.foundation.shape.CircleShape
                                )
                        )
                    }
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

            // Phần Phim Đang Chiếu
            MovieSection(
                title = "🎬 Phim Đang Chiếu",
                movies = nowPlaying,
                watchlist = watchlist,
                isLoading = isLoadingNowPlaying,
                onMovieClick = onMovieClick,
                onWatchlistClick = { viewModel.toggleWatchlist(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Phần Phim Phổ Biến
            MovieSection(
                title = "🔥 Phim Phổ Biến",
                movies = popular,
                watchlist = watchlist,
                isLoading = isLoadingPopular,
                onMovieClick = onMovieClick,
                onWatchlistClick = { viewModel.toggleWatchlist(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Nút tải lại khi không có dữ liệu
            if (nowPlaying.isEmpty() && popular.isEmpty() && !isLoadingNowPlaying && !isLoadingPopular) {
                Button(
                    onClick = { viewModel.refreshAll() },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(16.dp)
                ) {
                    Text("Tải lại dữ liệu")
                }
            }
        }

        // Hiển thị indicator khi đang pull-to-refresh
        PullRefreshIndicator(
            refreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter),
            backgroundColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun BannerItem(movie: Movie) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = movie.fullPosterUrl,
            contentDescription = movie.title,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
            error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
        )
        // Gradient mờ dần từ dưới lên
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f)),
                        startY = 300f
                    )
                )
        )
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(20.dp)
        ) {
            Text(
                text = movie.title,
                color = Color.White,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            movie.releaseDate?.let {
                Text(
                    text = "Khởi chiếu: $it",
                    color = Color.White.copy(alpha = 0.8f),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
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

        when {
            isLoading && movies.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(30.dp))
                }
            }
            movies.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Không có phim nào", style = MaterialTheme.typography.bodyMedium)
                }
            }
            else -> {
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
}
