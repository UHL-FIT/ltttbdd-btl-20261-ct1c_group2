package com.example.flickfind.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items as lazyItems
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flickfind.data.local.MovieEntity
import com.example.flickfind.data.local.SearchHistoryEntity
import com.example.flickfind.data.model.Genre
import com.example.flickfind.data.model.Movie
import com.example.flickfind.ui.viewmodel.MovieViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SearchScreen(
    viewModel: MovieViewModel,
    onMovieClick: (Movie) -> Unit,
    onLoginRequired: () -> Unit
) {
    var textFieldValue by remember { mutableStateOf(TextFieldValue("")) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val popularMovies by viewModel.popularMovies.collectAsState()
    val watchlist by viewModel.watchlist.collectAsState()
    val searchHistory by viewModel.searchHistory.collectAsState()
    val genres by viewModel.genres.collectAsState()
    val selectedGenreId by viewModel.selectedGenreId.collectAsState()
    val isLoadingSearch by viewModel.isLoadingSearch.collectAsState()
    
    val focusManager = LocalFocusManager.current

    // Debounce tìm kiếm cho việc gõ phím (700ms)
    LaunchedEffect(textFieldValue.text) {
        val queryText = textFieldValue.text
        if (queryText.isBlank()) {
            viewModel.searchMovies("")
        } else if (queryText != searchQuery) {
            delay(700)
            viewModel.searchMovies(queryText, saveToHistory = false)
        }
    }

    // Lọc theo genre ở client
    val baseMovies = if (searchQuery.isBlank()) popularMovies else searchResults
    val displayMovies = if (selectedGenreId != null) {
        baseMovies.filter { it.genreIds?.contains(selectedGenreId) == true }
    } else {
        baseMovies
    }
    
    val title = if (searchQuery.isBlank()) "Gợi ý phim hot" else "Kết quả cho \"$searchQuery\""
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

    SearchContent(
        textFieldValue = textFieldValue,
        onValueChange = { textFieldValue = it },
        onSearchAction = {
            val queryText = textFieldValue.text
            if (queryText.isNotBlank()) {
                viewModel.selectGenre(null) 
                viewModel.searchMovies(queryText, saveToHistory = true)
                focusManager.clearFocus()
            }
        },
        displayMovies = displayMovies,
        title = title,
        watchlist = watchlist,
        searchHistory = searchHistory,
        genres = genres,
        selectedGenreId = selectedGenreId,
        isLoadingSearch = isLoadingSearch,
        onGenreClick = { viewModel.selectGenre(if (selectedGenreId == it) null else it) },
        onMovieClick = onMovieClick,
        onWatchlistClick = { movie ->
            if (FirebaseAuth.getInstance().currentUser == null) {
                showLoginDialog = true
            } else {
                viewModel.toggleWatchlist(movie)
            }
        },
        onHistoryClick = { historyQuery ->
            textFieldValue = TextFieldValue(
                text = historyQuery,
                selection = TextRange(historyQuery.length)
            )
            viewModel.selectGenre(null)
            viewModel.searchMovies(historyQuery, saveToHistory = true)
            focusManager.clearFocus()
        },
        onDeleteHistory = { viewModel.deleteSearchQuery(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchContent(
    textFieldValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onSearchAction: () -> Unit = {},
    displayMovies: List<Movie>,
    title: String,
    watchlist: List<MovieEntity>,
    searchHistory: List<SearchHistoryEntity> = emptyList(),
    genres: List<Genre> = emptyList(),
    selectedGenreId: Int?,
    isLoadingSearch: Boolean = false,
    onGenreClick: (Int) -> Unit,
    onMovieClick: (Movie) -> Unit,
    onWatchlistClick: (Movie) -> Unit,
    onHistoryClick: (String) -> Unit = {},
    onDeleteHistory: (String) -> Unit = {}
) {
    var isHistoryExpanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = textFieldValue,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            placeholder = { Text("Tìm kiếm phim...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (textFieldValue.text.isNotEmpty()) {
                    IconButton(onClick = { onValueChange(TextFieldValue("")) }) {
                        Icon(Icons.Default.Close, contentDescription = "Xóa")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { onSearchAction() })
        )

        // Gợi ý thể loại
        if (genres.isNotEmpty()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                lazyItems(genres) { genre ->
                    FilterChip(
                        selected = selectedGenreId == genre.id,
                        onClick = { onGenreClick(genre.id) },
                        label = { Text(genre.name) }
                    )
                }
            }
        }

        // Trạng thái Loading
        if (isLoadingSearch) {
            Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }

        // Lịch sử tìm kiếm gần đây
        if (textFieldValue.text.isEmpty() && searchHistory.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Tìm kiếm gần đây",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                if (searchHistory.size > 5) {
                    TextButton(onClick = { isHistoryExpanded = !isHistoryExpanded }) {
                        Text(if (isHistoryExpanded) "Thu gọn" else "Xem thêm")
                    }
                }
            }
            
            val visibleHistory = if (isHistoryExpanded) searchHistory else searchHistory.take(5)

            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                lazyItems(visibleHistory) { history ->
                    ListItem(
                        headlineContent = { Text(history.query) },
                        leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                        trailingContent = {
                            IconButton(onClick = { onDeleteHistory(history.query) }) {
                                Icon(Icons.Default.Close, contentDescription = "Xóa", modifier = Modifier.size(16.dp))
                            }
                        },
                        modifier = Modifier.clickable { onHistoryClick(history.query) }
                    )
                }
            }
        } else {
            // Hiển thị kết quả phim
            if (displayMovies.isNotEmpty()) {
                Text(
                    text = title,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            } else if (!isLoadingSearch && textFieldValue.text.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Không tìm thấy phim nào khớp", color = MaterialTheme.colorScheme.outline)
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(displayMovies) { movie ->
                    val isFavorite = watchlist.any { it.id == movie.id }
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

@Preview(showBackground = true)
@Composable
fun SearchScreenPreview() {
    val mockMovies = listOf(
        Movie(1, "Phim tìm kiếm 1", null, 8.5, "2024-01-01", "Mô tả phim 1"),
        Movie(2, "Phim tìm kiếm 2", null, 7.2, "2024-01-02", "Mô tả phim 2")
    )
    SearchContent(
        textFieldValue = TextFieldValue("Deadpool"),
        onValueChange = {},
        displayMovies = mockMovies,
        title = "Kết quả tìm kiếm",
        watchlist = emptyList(),
        genres = emptyList(),
        selectedGenreId = null,
        onGenreClick = {},
        onMovieClick = {},
        onWatchlistClick = {}
    )
}
