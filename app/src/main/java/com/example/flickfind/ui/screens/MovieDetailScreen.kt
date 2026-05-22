package com.example.flickfind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flickfind.data.model.Movie
import com.example.flickfind.ui.viewmodel.MovieViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    onBackClick: () -> Unit
) {
    val movie by viewModel.selectedMovie.collectAsState()
    val isLoading by viewModel.isLoadingDetail.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    LaunchedEffect(movieId) {
        viewModel.fetchMovieDetails(movieId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(movie?.title ?: "Chi tiết phim") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                actions = {
                    movie?.let { currentMovie ->
                        val isFavorite = watchlist.any { it.id == currentMovie.id }
                        IconButton(onClick = { viewModel.toggleWatchlist(currentMovie) }) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Yêu thích",
                                tint = if (isFavorite) Color.Red else Color.Gray
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (errorMessage != null) {
                Column(
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.fetchMovieDetails(movieId) }) {
                        Text("Thử lại")
                    }
                }
            } else if (movie != null) {
                MovieDetailContent(movie!!)
            } else {
                Text(
                    text = "Không tìm thấy thông tin phim",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun MovieDetailContent(movie: Movie) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        AsyncImage(
            model = movie.fullPosterUrl,
            contentDescription = movie.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            contentScale = ContentScale.Crop,
            placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
            error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
        )

        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = movie.title,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color.Yellow)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${movie.voteAverage}/10",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = "Ngày khởi chiếu: ${movie.releaseDate ?: "N/A"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "Nội dung phim",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = movie.overview?.takeIf { it.isNotBlank() } ?: "Nội dung phim đang được cập nhật. Vui lòng quay lại sau!",
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp
            )

            if (!movie.credits?.cast.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Diễn viên",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(movie.credits!!.cast) { cast ->
                        Column(
                            modifier = Modifier.width(100.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = cast.fullProfileUrl,
                                contentDescription = cast.name,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
                                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = cast.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                            Text(
                                text = cast.character,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            if (!movie.productionCompanies.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Nhà sản xuất",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(movie.productionCompanies) { company ->
                        Column(
                            modifier = Modifier.width(120.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AsyncImage(
                                model = company.fullLogoUrl,
                                contentDescription = company.name,
                                modifier = Modifier
                                    .height(60.dp)
                                    .fillMaxWidth(),
                                contentScale = ContentScale.Fit,
                                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
                                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = company.name,
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }
    }
}
