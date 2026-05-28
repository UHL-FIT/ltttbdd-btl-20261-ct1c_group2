package com.example.flickfind.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import androidx.compose.ui.tooling.preview.Preview
import com.example.flickfind.data.model.Movie

@Composable
fun MovieCard(
    movie: Movie,
    isFavorite: Boolean = false,
    isWatched: Boolean = false,
    onMovieClick: (Movie) -> Unit,
    onWatchlistClick: (Movie) -> Unit,
    onWatchedClick: ((Boolean) -> Unit)? = null
) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onMovieClick(movie) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = movie.fullPosterUrl,
                    contentDescription = movie.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
                    error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
                )
                
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                ) {
                    if (onWatchedClick != null) {
                        IconButton(
                            onClick = { onWatchedClick(!isWatched) },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color.Black.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isWatched) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = "Mark as watched",
                                tint = if (isWatched) Color.Green else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    
                    IconButton(
                        onClick = { onWatchlistClick(movie) },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Watchlist",
                            tint = if (isFavorite) Color.Red else Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp),
                    color = Color.Black.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color.Yellow,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = movie.voteAverage?.toString() ?: "N/A",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Text(
                text = movie.title,
                modifier = Modifier.padding(8.dp),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun LoginRequiredDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yêu cầu đăng nhập") },
        text = { Text("Vui lòng đăng nhập để sử dụng tính năng này và lưu phim vào mục yêu thích của bạn.") },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Đăng nhập")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MovieCardPreview() {
    val sampleMovie = Movie(
        id = 1,
        title = "Deadpool & Wolverine",
        posterPath = "/8cdWjvZQUvS6Upt7TSRp9C9H0OK.jpg",
        voteAverage = 7.8,
        releaseDate = "2024-07-24",
        overview = "Một bộ phim hành động hài hước của Marvel."
    )
    MovieCard(
        movie = sampleMovie,
        isFavorite = true,
        onMovieClick = {},
        onWatchlistClick = {}
    )
}
