package com.example.newsapp_restapi.data.repository

import com.example.newsapp_restapi.BuildConfig
import com.example.newsapp_restapi.data.api.RetrofitInstance
import com.example.newsapp_restapi.data.model.Article
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NewsRepository {

    private val api    = RetrofitInstance.api
    private val apiKey = BuildConfig.NEWS_API_KEY

    sealed class Result<out T> {
        data class Success<T>(val data: T) : Result<T>()
        data class Error(val message: String) : Result<Nothing>()
    }

    suspend fun getTopHeadlines(
        country: String  = "us",
        category: String = "general",
        page: Int        = 1
    ): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTopHeadlines(
                country  = country,
                category = category,
                page     = page,
                apiKey   = apiKey
            )
            val articles = response.articles.filter {
                !it.title.isNullOrBlank() && it.title != "[Removed]"
            }
            Result.Success(articles)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }

    suspend fun searchNews(
        query: String,
        page: Int      = 1,
        sortBy: String = "publishedAt"
    ): Result<List<Article>> = withContext(Dispatchers.IO) {
        try {
            val response = api.searchNews(
                query  = query,
                page   = page,
                sortBy = sortBy,
                apiKey = apiKey
            )
            val articles = response.articles.filter {
                !it.title.isNullOrBlank() && it.title != "[Removed]"
            }
            Result.Success(articles)
        } catch (e: Exception) {
            Result.Error(e.localizedMessage ?: "Unknown error occurred")
        }
    }
}