package com.example.flickfind.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Về FlickFind") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // App Logo Icon representation
            Surface(
                modifier = Modifier.size(96.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "FlickFind Logo",
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "FlickFind",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "FlickFind là ứng dụng giải trí di động hiện đại giúp bạn khám phá thế giới điện ảnh phong phú. " +
                        "Ứng dụng cung cấp các thông tin chi tiết về các bộ phim đang chiếu, phim phổ biến, " +
                        "hỗ trợ tìm kiếm thông minh, xem trailer sắc nét và lưu danh sách phim yêu thích cá nhân tiện lợi.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Justify,
                lineHeight = 22.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Section 1: Tính năng nổi bật
            AboutSectionHeader(title = "Tính năng chính", icon = Icons.Default.Star)
            AboutFeatureItem(title = "Khám phá phim dễ dàng", description = "Xem danh sách phim đang chiếu và phim phổ biến cập nhật liên tục từ TMDB.")
            AboutFeatureItem(title = "Tìm kiếm thông minh", description = "Tìm phim theo từ khóa, lưu trữ lịch sử tìm kiếm cục bộ.")
            AboutFeatureItem(title = "Xem trailer chất lượng cao", description = "Tích hợp trình phát video trailer trực tiếp vô cùng mượt mà.")
            AboutFeatureItem(title = "Quản lý danh sách yêu thích", description = "Lưu phim yêu thích, đánh dấu trạng thái đã xem tiện lợi.")

            Spacer(modifier = Modifier.height(24.dp))

            // Section 2: Công nghệ sử dụng
            AboutSectionHeader(title = "Công nghệ phát triển", icon = Icons.Default.Code)
            AboutFeatureItem(title = "Ngôn ngữ & Giao diện", description = "Kotlin kết hợp Jetpack Compose cho giao diện hiện đại, mượt mà.")
            AboutFeatureItem(title = "Cơ sở dữ liệu Room Database", description = "Lưu trữ offline danh sách yêu thích, cache video trailer và lịch sử tìm kiếm.")
            AboutFeatureItem(title = "Mạng & APIs", description = "Sử dụng Retrofit, OkHttp kết nối và đồng bộ dữ liệu từ TMDB API.")
            AboutFeatureItem(title = "Firebase Cloud", description = "Đăng nhập và đồng bộ hóa đám mây thông qua Firebase Authentication và Firestore.")

            Spacer(modifier = Modifier.height(24.dp))

            // Section 3: Đội ngũ phát triển
            AboutSectionHeader(title = "Đội ngũ phát triển", icon = Icons.Default.Group)
            
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Nhóm sinh viên",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\n• Nguyễn Xuân Kiên\n• Phạm Công Nghĩa\n• Trần Thanh Huyền\n• Bùi Đình Dũng",
                        textAlign = TextAlign.Justify,
                        style = MaterialTheme.typography.bodyMedium,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "KHOA CNTT - TRƯỜNG ĐẠI HỌC HẠ LONG",
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AboutSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
fun AboutFeatureItem(title: String, description: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "• $title",
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}
