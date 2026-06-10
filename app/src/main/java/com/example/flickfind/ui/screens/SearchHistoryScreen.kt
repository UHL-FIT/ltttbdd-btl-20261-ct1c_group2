package com.example.flickfind.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.flickfind.ui.viewmodel.MovieViewModel

@Composable
fun SearchHistoryScreen(
    viewModel: MovieViewModel,
    onHistoryClick: (String) -> Unit,
    onDeleteHistory: (String) -> Unit
) {
    val historyList by viewModel.searchHistory.collectAsState()
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Lịch sử tìm kiếm",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            items(historyList) { history ->
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
                HorizontalDivider()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SearchHistoryScreenPreview() {
    // Preview with dummy data
    val dummyViewModel = androidx.lifecycle.viewmodel.compose.viewModel<MovieViewModel>(factory = androidx.lifecycle.ViewModelProvider.NewInstanceFactory())
    SearchHistoryScreen(viewModel = dummyViewModel, onHistoryClick = {}, onDeleteHistory = {})
}
