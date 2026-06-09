package com.example.newsapp_restapi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newsapp_restapi.data.model.Article
import com.example.newsapp_restapi.ui.component.NewsCard
import com.example.newsapp_restapi.ui.component.NewsCardSkeleton
import com.example.newsapp_restapi.viewmodel.NewsUiState
import com.example.newsapp_restapi.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    uiState: NewsUiState,
    viewModel: NewsViewModel,
    onArticleClick: (Article) -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val listState      = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total       = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3
                    && !uiState.isLoadingSearch
                    && uiState.hasMoreSearch
                    && uiState.searchQuery.isNotBlank()
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreSearch()
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { viewModel.clearSearch(); onBack() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, "Back")
                    }
                },
                title = {
                    OutlinedTextField(
                        value         = uiState.searchQuery,
                        onValueChange = { viewModel.onSearchQueryChange(it) },
                        placeholder   = { Text("Search news...", fontSize = 14.sp) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth().focusRequester(focusRequester),
                        leadingIcon   = {
                            Icon(Icons.Outlined.Search, null, modifier = Modifier.size(18.dp))
                        },
                        trailingIcon  = {
                            if (uiState.searchQuery.isNotBlank()) {
                                IconButton(onClick = { viewModel.clearSearch() }) {
                                    Icon(Icons.Outlined.Clear, "Clear", modifier = Modifier.size(18.dp))
                                }
                            }
                        },
                        shape  = MaterialTheme.shapes.extraLarge,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedBorderColor   = MaterialTheme.colorScheme.primary
                        ),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            when {
                uiState.searchQuery.isBlank() -> SearchEmptyPrompt()

                uiState.isLoadingSearch && uiState.searchResults.isEmpty() -> {
                    LazyColumn(
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(5) { NewsCardSkeleton() }
                    }
                }

                uiState.searchError != null && uiState.searchResults.isEmpty() -> ErrorScreen(
                    message = uiState.searchError,
                    onRetry = { viewModel.retrySearch() }
                )

                !uiState.isLoadingSearch && uiState.searchResults.isEmpty() ->
                    SearchNoResults(query = uiState.searchQuery)

                else -> {
                    LazyColumn(
                        state               = listState,
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier            = Modifier.fillMaxSize()
                    ) {
                        item {
                            Text(
                                text  = "${uiState.searchResults.size}+ results for \"${uiState.searchQuery}\"",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        items(uiState.searchResults) { article ->
                            NewsCard(article = article, onClick = { onArticleClick(article) })
                        }

                        if (uiState.isLoadingSearch) {
                            items(3) { NewsCardSkeleton() }
                        }

                        if (uiState.searchError != null && uiState.searchResults.isNotEmpty()) {
                            item {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text     = uiState.searchError,
                                        style    = MaterialTheme.typography.bodySmall,
                                        color    = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.loadMoreSearch() }) {
                                        Text("Retry")
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

@Composable
private fun SearchEmptyPrompt() {
    Column(
        modifier              = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text("🔍", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Search for news",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "Type a keyword to search from thousands of news sources.",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun SearchNoResults(query: String) {
    Column(
        modifier              = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text("📭", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "No results found",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text      = "No articles found for \"$query\". Try a different keyword.",
            style     = MaterialTheme.typography.bodySmall,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}