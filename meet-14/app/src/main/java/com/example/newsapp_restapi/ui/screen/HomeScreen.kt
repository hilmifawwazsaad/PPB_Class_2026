package com.example.newsapp_restapi.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.newsapp_restapi.data.model.Article
import com.example.newsapp_restapi.ui.component.NewsCard
import com.example.newsapp_restapi.ui.component.NewsCardFeatured
import com.example.newsapp_restapi.ui.component.NewsCardSkeleton
import com.example.newsapp_restapi.viewmodel.NewsUiState
import com.example.newsapp_restapi.viewmodel.NewsViewModel

@OptIn(ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    uiState: NewsUiState,
    viewModel: NewsViewModel,
    onArticleClick: (Article) -> Unit,
    onSearchClick: () -> Unit
) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = uiState.isRefreshing,
        onRefresh  = { viewModel.refreshHeadlines() }
    )
    val listState = rememberLazyListState()

    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val total       = listState.layoutInfo.totalItemsCount
            lastVisible >= total - 3 && !uiState.isLoadingHeadlines && uiState.hasMoreHeadlines
        }
    }
    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore) viewModel.loadMoreHeadlines()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("NewsApp", fontWeight = FontWeight.Black, fontSize = 22.sp)
                        Text(
                            text  = "Stay informed, always",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Outlined.Search, contentDescription = "Search")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .pullRefresh(pullRefreshState)
        ) {
            when {
                uiState.isLoadingHeadlines && uiState.headlines.isEmpty() -> LoadingScreen()

                uiState.headlinesError != null && uiState.headlines.isEmpty() -> ErrorScreen(
                    message = uiState.headlinesError,
                    onRetry = { viewModel.retryHeadlines() }
                )

                else -> {
                    LazyColumn(
                        state               = listState,
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        item {
                            CategoryChips(
                                categories       = viewModel.categories,
                                selectedCategory = uiState.selectedCategory,
                                onCategorySelect = { viewModel.selectCategory(it) }
                            )
                        }

                        if (uiState.headlines.size >= 3) {
                            item {
                                FeaturedSection(
                                    articles       = uiState.headlines.take(3),
                                    onArticleClick = onArticleClick
                                )
                            }
                        }

                        item {
                            Text(
                                text     = "Latest News",
                                style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                            )
                        }

                        itemsIndexed(
                            items = if (uiState.headlines.size >= 3) uiState.headlines.drop(3)
                            else uiState.headlines
                        ) { _, article ->
                            NewsCard(
                                article  = article,
                                onClick  = { onArticleClick(article) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }

                        if (uiState.isLoadingHeadlines && uiState.headlines.isNotEmpty()) {
                            items(3) {
                                NewsCardSkeleton(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }

                        if (uiState.headlinesError != null && uiState.headlines.isNotEmpty()) {
                            item {
                                Row(
                                    modifier              = Modifier.fillMaxWidth().padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment     = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text     = uiState.headlinesError,
                                        style    = MaterialTheme.typography.bodySmall,
                                        color    = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.weight(1f)
                                    )
                                    TextButton(onClick = { viewModel.loadMoreHeadlines() }) {
                                        Text("Retry")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            PullRefreshIndicator(
                refreshing   = uiState.isRefreshing,
                state        = pullRefreshState,
                modifier     = Modifier.align(Alignment.TopCenter),
                contentColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CategoryChips(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelect: (String) -> Unit
) {
    LazyRow(
        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            FilterChip(
                selected = category == selectedCategory,
                onClick  = { onCategorySelect(category) },
                label    = {
                    Text(
                        text     = category.replaceFirstChar { it.uppercase() },
                        fontSize = 13.sp
                    )
                }
            )
        }
    }
}

@Composable
private fun FeaturedSection(
    articles: List<Article>,
    onArticleClick: (Article) -> Unit
) {
    Column {
        Text(
            text     = "Top Stories",
            style    = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding        = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(articles) { article ->
                NewsCardFeatured(
                    article = article,
                    onClick = { onArticleClick(article) }
                )
            }
        }
    }
}

@Composable
fun LoadingScreen() {
    LazyColumn(
        contentPadding      = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier            = Modifier.fillMaxSize()
    ) {
        items(5) { NewsCardSkeleton() }
    }
}

@Composable
fun ErrorScreen(message: String?, onRetry: () -> Unit) {
    Column(
        modifier              = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment   = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Center
    ) {
        Text("😕", style = MaterialTheme.typography.displayMedium)
        Spacer(Modifier.height(16.dp))
        Text(
            text  = "Oops! Something went wrong",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text  = message ?: "Unknown error occurred",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry) { Text("Try Again") }
    }
}