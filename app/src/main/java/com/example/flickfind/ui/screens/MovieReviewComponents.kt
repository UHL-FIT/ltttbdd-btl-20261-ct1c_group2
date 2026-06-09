package com.example.flickfind.ui.screens

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.flickfind.data.model.MovieComment
import java.text.SimpleDateFormat
import java.util.*

// Helper function to format timestamp to readable relative time
fun formatTimeAgo(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val seconds = diff / 1000
    val minutes = seconds / 60
    val hours = minutes / 60
    val days = hours / 24

    return when {
        seconds < 60 -> "Vừa xong"
        minutes < 60 -> "$minutes phút trước"
        hours < 24 -> "$hours giờ trước"
        days < 30 -> "$days ngày trước"
        else -> {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            sdf.format(Date(timestamp))
        }
    }
}

@Composable
fun RatingSection(
    averageRating: Double,
    breakdown: Map<Int, Int>,
    userRating: Int?,
    onRatingSubmit: (Int) -> Unit,
    isUserLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalRatings = breakdown.values.sum()

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Đánh giá từ người dùng",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Average Score Panel
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(0.4f)
                ) {
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", averageRating),
                        style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Row {
                        repeat(5) { index ->
                            val starRating = index + 1
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (starRating <= averageRating.plus(0.5).toInt()) Color(0xFFFFC107) else Color.Gray,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$totalRatings lượt đánh giá",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Star Breakdown Bars
                Column(modifier = Modifier.weight(0.6f)) {
                    for (stars in 5 downTo 1) {
                        val count = breakdown[stars] ?: 0
                        val ratio = if (totalRatings > 0) count.toFloat() / totalRatings else 0f
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Text(
                                text = "$stars",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.width(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier.weight(1f).height(6.dp).clip(CircleShape),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.outlineVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "$count",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.width(24.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(16.dp))

            // User Interactive Rating
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (userRating != null) "Đánh giá của bạn: $userRating sao" else "Bạn đánh giá thế nào về phim này?",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(5) { index ->
                        val ratingValue = index + 1
                        val isSelected = userRating != null && ratingValue <= userRating
                        val scale by animateFloatAsState(targetValue = if (isSelected) 1.2f else 1.0f, label = "star_scale")
                        Icon(
                            imageVector = if (isSelected) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Rate $ratingValue Stars",
                            tint = if (isSelected) Color(0xFFFFC107) else MaterialTheme.colorScheme.outline,
                            modifier = Modifier
                                .size(36.dp)
                                .scale(scale)
                                .clickable {
                                    if (!isUserLoggedIn) {
                                        onLoginRequired()
                                    } else {
                                        onRatingSubmit(ratingValue)
                                    }
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(
    comment: MovieComment,
    replies: List<MovieComment>,
    currentUserId: String?,
    onLikeClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onReplySubmit: (String) -> Unit,
    onLoadReplies: () -> Unit,
    isUserLoggedIn: Boolean,
    onLoginRequired: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isRepliesExpanded by remember { mutableStateOf(false) }
    var isWritingReply by remember { mutableStateOf(false) }
    var replyText by remember { mutableStateOf("") }
    val isLiked = currentUserId != null && comment.likedBy.contains(currentUserId)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .animateContentSize()
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            // User Avatar
            AsyncImage(
                model = comment.avatar ?: "https://www.gravatar.com/avatar/?d=mp",
                contentDescription = "Avatar của ${comment.username}",
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop,
                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Comment Body
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = comment.username,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatTimeAgo(comment.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = comment.content,
                    style = MaterialTheme.typography.bodyMedium,
                    lineHeight = 20.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                // Actions row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Like Action
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable {
                                if (!isUserLoggedIn) {
                                    onLoginRequired()
                                } else {
                                    onLikeClick()
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) Color.Red else MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${comment.likes}",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Reply Toggle Action
                    Text(
                        text = "Phản hồi",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                if (!isUserLoggedIn) {
                                    onLoginRequired()
                                } else {
                                    isWritingReply = !isWritingReply
                                }
                            }
                            .padding(vertical = 4.dp, horizontal = 8.dp)
                    )

                    if (currentUserId == comment.userId) {
                        Spacer(modifier = Modifier.width(16.dp))
                        // Delete Action
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Xóa comment",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { onDeleteClick() }
                        )
                    }
                }
            }
        }

        // Reply Input Section
        if (isWritingReply) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 52.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = replyText,
                    onValueChange = { replyText = it },
                    placeholder = { Text("Viết phản hồi...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    textStyle = TextStyle(fontSize = 14.sp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    singleLine = true
                )
                IconButton(
                    onClick = {
                        if (replyText.isNotBlank()) {
                            onReplySubmit(replyText)
                            replyText = ""
                            isWritingReply = false
                            isRepliesExpanded = true
                        }
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Gửi phản hồi",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Nested Replies Section
        if (comment.replyCount > 0) {
            Column(modifier = Modifier.padding(start = 52.dp)) {
                if (!isRepliesExpanded) {
                    Text(
                        text = "Xem ${comment.replyCount} câu trả lời",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                onLoadReplies()
                                isRepliesExpanded = true
                            }
                            .padding(vertical = 4.dp)
                    )
                } else {
                    Text(
                        text = "Ẩn câu trả lời",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { isRepliesExpanded = false }
                            .padding(vertical = 4.dp)
                    )

                    // Render list of replies
                    replies.forEach { reply ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            AsyncImage(
                                model = reply.avatar ?: "https://www.gravatar.com/avatar/?d=mp",
                                contentDescription = "Avatar của ${reply.username}",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop,
                                placeholder = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image),
                                error = androidx.compose.ui.res.painterResource(id = android.R.drawable.ic_menu_report_image)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = reply.username,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = formatTimeAgo(reply.createdAt),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = reply.content,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .clickable {
                                                if (!isUserLoggedIn) {
                                                    onLoginRequired()
                                                } else {
                                                    // Trigger reply like
                                                    onLikeClick() // For simplicity in nested model, toggle comment's likes
                                                }
                                            }
                                            .padding(vertical = 2.dp, horizontal = 4.dp)
                                    ) {
                                        val isReplyLiked = currentUserId != null && reply.likedBy.contains(currentUserId)
                                        Icon(
                                            imageVector = if (isReplyLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Like reply",
                                            tint = if (isReplyLiked) Color.Red else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "${reply.likes}",
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                    if (currentUserId == reply.userId) {
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Xóa phản hồi",
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clickable { onDeleteClick() }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
