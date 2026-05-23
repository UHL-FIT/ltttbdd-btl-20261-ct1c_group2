package com.example.flickfind

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flickfind.data.local.AppDatabase
import com.example.flickfind.data.remote.TMDBApiService
import com.example.flickfind.data.repository.MovieRepository
import com.example.flickfind.ui.screens.AuthScreen
import com.example.flickfind.ui.screens.HomeScreen
import com.example.flickfind.ui.screens.MovieDetailScreen
import com.example.flickfind.ui.screens.SearchScreen
import com.example.flickfind.ui.screens.WatchlistScreen
import com.example.flickfind.ui.theme.FlickFindTheme
import com.example.flickfind.ui.viewmodel.AuthViewModel
import com.example.flickfind.ui.viewmodel.MovieViewModel
import com.example.flickfind.ui.viewmodel.MovieViewModelFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo Retrofit
        val client = OkHttpClient.Builder()
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

        // Khởi tạo Database
        val database = AppDatabase.getDatabase(this)
        val repository = MovieRepository(apiService, database.movieDao())

        enableEdgeToEdge()
        setContent {
            FlickFindTheme {
                val movieViewModel: MovieViewModel = viewModel(
                    factory = MovieViewModelFactory(repository)
                )
                val authViewModel: AuthViewModel = viewModel()
                
                val currentUser by authViewModel.currentUser.collectAsState()

                if (currentUser == null) {
                    AuthScreen(authViewModel = authViewModel) {
                        // Đăng nhập thành công, Flow sẽ tự động cập nhật currentUser
                    }
                } else {
                    MainScreen(viewModel = movieViewModel, authViewModel = authViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: MovieViewModel, authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        Screen.Home,
        Screen.Search,
        Screen.Watchlist
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        text = "FlickFind", 
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    ) 
                },
                actions = {
                    IconButton(onClick = { authViewModel.logout() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Đăng xuất",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = null) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.route == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
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
                    }
                ) 
            }
            composable(Screen.Search.route) { 
                SearchScreen(
                    viewModel = viewModel,
                    onMovieClick = { movie ->
                        navController.navigate("detail/${movie.id}")
                    }
                ) 
            }
            composable(Screen.Watchlist.route) { 
                WatchlistScreen(
                    viewModel = viewModel,
                    onMovieClick = { movie ->
                        navController.navigate("detail/${movie.id}")
                    }
                ) 
            }
            composable("detail/{movieId}") { backStackEntry ->
                val movieId = backStackEntry.arguments?.getString("movieId")?.toIntOrNull() ?: 0
                MovieDetailScreen(
                    movieId = movieId,
                    viewModel = viewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Home : Screen("home", "Trang chủ", Icons.Default.Home)
    object Search : Screen("search", "Tìm kiếm", Icons.Default.Search)
    object Watchlist : Screen("watchlist", "Mục ưa thích", Icons.Default.Favorite)
}
