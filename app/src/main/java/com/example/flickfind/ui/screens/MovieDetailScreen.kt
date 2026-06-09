package com.example.flickfind.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.flickfind.data.model.Movie
import com.example.flickfind.data.model.Video
import com.example.flickfind.data.repository.ReviewRepository
import com.example.flickfind.ui.viewmodel.MovieReviewViewModel
import com.example.flickfind.ui.viewmodel.MovieReviewViewModelFactory
import com.example.flickfind.ui.viewmodel.MovieViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailScreen(
    movieId: Int,
    viewModel: MovieViewModel,
    onBackClick: () -> Unit,
    onLoginRequired: () -> Unit
) {
    val movie by viewModel.selectedMovie.collectAsState()
    val videos by viewModel.movieVideos.collectAsState()
    val isLoading by viewModel.isLoadingDetail.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    
    var selectedVideoKey by remember { mutableStateOf<String?>(null) }
    var showAllTrailers by remember { mutableStateOf(false) }
    var showLoginDialog by remember { mutableStateOf(false) }

    if (showLoginDialog) {
        LoginRequiredDialog(
            onDismiss = { showLoginDialog = false },
            onConfirm = {
                showLoginDialog = false
                onLoginRequired()
            }
        )
    }

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
                        IconButton(onClick = { 
                            if (FirebaseAuth.getInstance().currentUser == null) {
                                showLoginDialog = true
                            } else {
                                viewModel.toggleWatchlist(currentMovie)
                            }
                        }) {
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
                // Initialize Review ViewModel
                val reviewViewModel: MovieReviewViewModel = viewModel(
                    factory = MovieReviewViewModelFactory(ReviewRepository())
                )
                // Observe state flows
                val ratingStats by reviewViewModel.ratingStats.collectAsState()
                val userRating by reviewViewModel.userRating.collectAsState()
                val commentList by reviewViewModel.comments.collectAsState()
                val repliesMap by reviewViewModel.replies.collectAsState()
                val isUserLoggedIn = FirebaseAuth.getInstance().currentUser != null

                // Set movie ID for review data
                LaunchedEffect(movieId) {
                    reviewViewModel.setMovieId(movieId.toString())
                }

                MovieDetailContent(
                    movie = movie!!,
                    videos = videos,
                    onPlayVideo = { key -> selectedVideoKey = key },
                    onShowAllTrailers = { showAllTrailers = true },
                    ratingSection = {
                        RatingSection(
                            averageRating = ratingStats.first,
                            breakdown = ratingStats.second,
                            userRating = userRating,
                            onRatingSubmit = { rating -> reviewViewModel.submitRating(rating) },
                            isUserLoggedIn = isUserLoggedIn,
                            onLoginRequired = { showLoginDialog = true }
                        )
                    },
                    commentsSection = {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Bình luận (${commentList.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Comment Input Field for new comments
                            var newCommentText by remember { mutableStateOf("") }
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = newCommentText,
                                    onValueChange = { newCommentText = it },
                                    placeholder = { Text("Viết bình luận của bạn...") },
                                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                                    shape = RoundedCornerShape(24.dp),
                                    singleLine = true
                                )
                                IconButton(
                                    onClick = {
                                        if (newCommentText.isNotBlank()) {
                                            if (!isUserLoggedIn) {
                                                showLoginDialog = true
                                            } else {
                                                reviewViewModel.submitComment(newCommentText)
                                                newCommentText = ""
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(48.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = "Gửi bình luận",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }

                            if (commentList.isEmpty()) {
                                Text(
                                    text = "Chưa có bình luận nào. Hãy là người đầu tiên bình luận!",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                commentList.forEach { comment ->
                                    CommentItem(
                                        comment = comment,
                                        replies = repliesMap[comment.id] ?: emptyList(),
                                        currentUserId = FirebaseAuth.getInstance().currentUser?.uid,
                                        onLikeClick = { reviewViewModel.toggleLikeComment(comment.id) },
                                        onDeleteClick = { reviewViewModel.deleteComment(comment.id, comment.parentId) },
                                        onReplySubmit = { replyText -> reviewViewModel.submitReply(comment.id, replyText) },
                                        onLoadReplies = { reviewViewModel.loadReplies(comment.id) },
                                        isUserLoggedIn = isUserLoggedIn,
                                        onLoginRequired = { showLoginDialog = true }
                                    )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                )
            } else {
                Text(
                    text = "Không tìm thấy thông tin phim",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
    
    // Video Player Dialog
    if (selectedVideoKey != null) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { selectedVideoKey = null },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        ) {
            VideoPlayerDialog(
                videoKey = selectedVideoKey!!,
                onDismiss = { selectedVideoKey = null }
            )
        }
    }
    
    // Bottom Sheet for all trailers
    if (showAllTrailers) {
        ModalBottomSheet(onDismissRequest = { showAllTrailers = false }) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tất cả Trailers & Teasers", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(videos) { video ->
                        VideoThumbnail(video = video, onClick = { selectedVideoKey = video.key })
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
fun MovieDetailContent(
    movie: Movie,
    videos: List<Video> = emptyList(),
    onPlayVideo: (String) -> Unit = {},
    onShowAllTrailers: () -> Unit = {},
    ratingSection: @Composable () -> Unit = {},
    commentsSection: @Composable () -> Unit = {}
) {
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
            
            // Trailers Section
            if (videos.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Trailers & Teasers",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (videos.size > 3) {
                        TextButton(onClick = onShowAllTrailers) {
                            Text("Xem thêm")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(videos.take(3)) { video ->
                        VideoThumbnail(video = video, onClick = { onPlayVideo(video.key) })
                    }
                }
            }

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
            
            Spacer(modifier = Modifier.height(24.dp))
            ratingSection()
            Spacer(modifier = Modifier.height(24.dp))
            commentsSection()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun VideoThumbnail(video: Video, onClick: () -> Unit) {
    Column(modifier = Modifier.width(200.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .clip(MaterialTheme.shapes.medium),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
            )
            
            // Overlay gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f))
            )
            
            // Play Icon
            IconButton(onClick = onClick) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play Trailer",
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            // Badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(8.dp)
                    .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.type,
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = video.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
    }
}
