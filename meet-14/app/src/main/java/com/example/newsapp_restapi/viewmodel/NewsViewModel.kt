package com.example.newsapp_restapi.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.newsapp_restapi.data.model.Article
import com.example.newsapp_restapi.data.repository.NewsRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class NewsUiState(
    val headlines: List<Article>     = emptyList(),
    val searchResults: List<Article> = emptyList(),
    val isLoadingHeadlines: Boolean  = false,
    val isLoadingSearch: Boolean     = false,
    val isRefreshing: Boolean        = false,
    val headlinesError: String?      = null,
    val searchError: String?         = null,
    val selectedCategory: String     = "general",
    val searchQuery: String          = "",
    val currentPage: Int             = 1,
    val searchPage: Int              = 1,
    val hasMoreHeadlines: Boolean    = true,
    val hasMoreSearch: Boolean       = true,
)

@OptIn(FlowPreview::class)
class NewsViewModel(
    private val repository: NewsRepository = NewsRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(NewsUiState())
    val uiState: StateFlow<NewsUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    val categories = listOf(
        "general", "business", "technology",
        "sports", "entertainment", "health", "science"
    )

    init {
        loadTopHeadlines()
    }

    // ── Top Headlines ─────────────────────────────────────────────────────────

    fun loadTopHeadlines(reset: Boolean = false) {
        if (_uiState.value.isLoadingHeadlines && !reset) return
        val page = if (reset) 1 else _uiState.value.currentPage

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHeadlines = true, headlinesError = null) }

            when (val result = repository.getTopHeadlines(
                category = _uiState.value.selectedCategory,
                page     = page
            )) {
                is NewsRepository.Result.Success -> {
                    _uiState.update { state ->
                        val newList = if (reset || page == 1) result.data
                        else state.headlines + result.data
                        state.copy(
                            headlines          = newList,
                            isLoadingHeadlines = false,
                            currentPage        = page + 1,
                            hasMoreHeadlines   = result.data.size >= 20
                        )
                    }
                }
                is NewsRepository.Result.Error -> {
                    _uiState.update {
                        it.copy(isLoadingHeadlines = false, headlinesError = result.message)
                    }
                }
            }
        }
    }

    fun loadMoreHeadlines() {
        if (!_uiState.value.isLoadingHeadlines && _uiState.value.hasMoreHeadlines)
            loadTopHeadlines()
    }

    // ── Pull to Refresh ───────────────────────────────────────────────────────

    fun refreshHeadlines() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, currentPage = 1) }
            when (val result = repository.getTopHeadlines(
                category = _uiState.value.selectedCategory,
                page     = 1
            )) {
                is NewsRepository.Result.Success -> _uiState.update {
                    it.copy(
                        headlines        = result.data,
                        isRefreshing     = false,
                        currentPage      = 2,
                        hasMoreHeadlines = result.data.size >= 20,
                        headlinesError   = null
                    )
                }
                is NewsRepository.Result.Error -> _uiState.update {
                    it.copy(isRefreshing = false, headlinesError = result.message)
                }
            }
        }
    }

    // ── Category ──────────────────────────────────────────────────────────────

    fun selectCategory(category: String) {
        if (_uiState.value.selectedCategory == category) return
        _uiState.update {
            it.copy(
                selectedCategory = category,
                currentPage      = 1,
                headlines        = emptyList(),
                hasMoreHeadlines = true,
                headlinesError   = null
            )
        }
        loadTopHeadlines(reset = true)
    }

    // ── Search ────────────────────────────────────────────────────────────────

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()

        if (query.isBlank()) {
            _uiState.update {
                it.copy(searchResults = emptyList(), searchError = null, isLoadingSearch = false)
            }
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // debounce
            searchNews(query = query, reset = true)
        }
    }

    fun searchNews(query: String = _uiState.value.searchQuery, reset: Boolean = false) {
        if (query.isBlank()) return
        val page = if (reset) 1 else _uiState.value.searchPage

        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingSearch = true, searchError = null) }

            when (val result = repository.searchNews(query = query, page = page)) {
                is NewsRepository.Result.Success -> {
                    _uiState.update { state ->
                        val newList = if (reset || page == 1) result.data
                        else state.searchResults + result.data
                        state.copy(
                            searchResults   = newList,
                            isLoadingSearch = false,
                            searchPage      = page + 1,
                            hasMoreSearch   = result.data.size >= 20
                        )
                    }
                }
                is NewsRepository.Result.Error -> _uiState.update {
                    it.copy(isLoadingSearch = false, searchError = result.message)
                }
            }
        }
    }

    fun loadMoreSearch() {
        val s = _uiState.value
        if (!s.isLoadingSearch && s.hasMoreSearch && s.searchQuery.isNotBlank())
            searchNews(reset = false)
    }

    fun clearSearch() {
        searchJob?.cancel()
        _uiState.update {
            it.copy(
                searchQuery     = "",
                searchResults   = emptyList(),
                searchError     = null,
                isLoadingSearch = false,
                searchPage      = 1,
                hasMoreSearch   = true
            )
        }
    }

    fun retryHeadlines() {
        _uiState.update { it.copy(currentPage = 1, headlines = emptyList()) }
        loadTopHeadlines(reset = true)
    }

    fun retrySearch() = searchNews(reset = true)
}