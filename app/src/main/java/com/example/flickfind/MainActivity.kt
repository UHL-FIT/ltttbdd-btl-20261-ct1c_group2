package com.example.flickfind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flickfind.data.local.AppDatabase
import com.example.flickfind.data.remote.TMDBApiService
import com.example.flickfind.data.repository.MovieRepository
import com.example.flickfind.data.repository.AuthRepository
import com.example.flickfind.ui.screens.*
import com.example.flickfind.ui.theme.FlickFindTheme
import com.example.flickfind.ui.viewmodel.AuthViewModel
import com.example.flickfind.ui.viewmodel.AuthViewModelFactory
import com.example.flickfind.ui.viewmodel.MovieViewModel
import com.example.flickfind.ui.viewmodel.MovieViewModelFactory
import java.util.concurrent.TimeUnit
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo Retrofit
        // Ép buộc không sử dụng Gzip để tránh lỗi "gzip finished without exhausting source"
        // Tăng timeout lên 30s để tránh "Request timed out"
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("Accept-Encoding", "identity")
                    .build()
                chain.proceed(request)
            }
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(TMDBApiService.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(TMDBApiService::class.java)

        // Khởi tạo Database & Repositories
        val database = AppDatabase.getDatabase(this)
        val movieRepository = MovieRepository(apiService, database.movieDao(), this)
        val authRepository = AuthRepository()

        enableEdgeToEdge()
        setContent {
            FlickFindTheme {
                val movieViewModel: MovieViewModel = viewModel(
                    factory = MovieViewModelFactory(movieRepository)
                )
                val authViewModel: AuthViewModel = viewModel(
                    factory = AuthViewModelFactory(authRepository)
                )
                
                val user by authViewModel.user.collectAsState()

                LaunchedEffect(user?.uid) {
                    movieViewModel.loadWatchlistForUser(user?.uid)
                    // Làm mới dữ liệu phim khi người dùng thay đổi (đăng nhập/đăng xuất)
                    movieViewModel.refreshAll()
                }

                MainScreen(movieViewModel, authViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: MovieViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val user by authViewModel.user.collectAsState()
    
    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Watchlist,
        Screen.Profile
    )

    Scaffold(
        bottomBar = {
            if (currentDestination?.route in items.map { it.route }) {
                NavigationBar {
                    items.forEach { screen ->
                        NavigationBarItem(
                            icon = { Icon(screen.icon, contentDescription = null) },
                            label = { Text(screen.title) },
                            selected = currentDestination?.route == screen.route,
                            onClick = {
                                val popped = navController.popBackStack(screen.route, inclusive = false)
                                if (!popped) {
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) { 
                HomeScreen(
                    viewModel = viewModel,
                    onMovieClick = { movie -> 
                        navController.navigate("detail/${movie.id}")
                    },
                    onLoginRequired = { navController.navigate("login") }
                ) 
            }
            composable(Screen.Search.route) { 
                SearchScreen(
                    viewModel = viewModel,
                    onMovieClick = { movie ->
                        navController.navigate("detail/${movie.id}")
                    },
                    onLoginRequired = { navController.navigate("login") }
                ) 
            }
            composable("search_history") { 
                SearchHistoryScreen(
                    viewModel = viewModel,
                    onHistoryClick = { query ->
                        viewModel.selectGenre(null)
                        viewModel.searchMovies(query, saveToHistory = true)
                        navController.navigate(Screen.Search.route)
                    },
                    onDeleteHistory = { viewModel.deleteSearchQuery(it) }
                )
            }
            composable(Screen.Watchlist.route) { 
                WatchlistScreen(
                    viewModel = viewModel,
                    onMovieClick = { movie -> 
                        navController.navigate("detail/${movie.id}")
                    },
                    onLoginClick = { navController.navigate("login") }
                ) 
            }
            composable(Screen.Profile.route) { 
                ProfileScreen(
                    authViewModel = authViewModel,
                    movieViewModel = viewModel,
                    onLogout = {
                        navController.popBackStack()
                    },
                    onNavigateToEditProfile = { navController.navigate("edit_profile") },
                    onNavigateToWatchlist = { navController.navigate(Screen.Watchlist.route) },
                    onLoginClick = { navController.navigate("login") },
                    onNavigateToSearch = {
                        navController.navigate(Screen.Search.route)
                    },
                    onNavigateToAbout = {
                        navController.navigate("about")
                    }
                )
            }
            composable("edit_profile") {
                EditProfileScreen(
                    viewModel = authViewModel,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("about") {
                AboutScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            composable("detail/{movieId}") { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull() ?: 0
                MovieDetailScreen(
                    movieId = movieId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() },
                    onLoginRequired = { navController.navigate("login") }
                )
            }
            // Tích hợp Login/Register vào đây
            composable("login") {
                LoginScreen(
                    viewModel = authViewModel,
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = { 
                        navController.popBackStack() 
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    viewModel = authViewModel,
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = { 
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    object Search : Screen("search", "Tìm kiếm", Icons.Default.Search)
    object Watchlist : Screen("watchlist", "Mục ưa thích", Icons.Default.Favorite)
    object Profile : Screen("profile", "Cá nhân", Icons.Default.AccountCircle)
}
