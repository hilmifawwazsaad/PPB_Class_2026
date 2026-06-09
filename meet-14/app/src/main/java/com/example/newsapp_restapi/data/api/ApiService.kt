package com.example.newsapp_restapi.data.api

import com.example.newsapp_restapi.data.model.NewsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {

    @GET("top-headlines")
    suspend fun getTopHeadlines(
        @Query("country")  country: String  = "us",
        @Query("category") category: String = "general",
        @Query("pageSize") pageSize: Int    = 20,
        @Query("page")     page: Int        = 1,
        @Query("apiKey")   apiKey: String
    ): NewsResponse

    @GET("everything")
    suspend fun searchNews(
        @Query("q")        query: String,
        @Query("pageSize") pageSize: Int    = 20,
        @Query("page")     page: Int        = 1,
        @Query("sortBy")   sortBy: String   = "publishedAt",
        @Query("apiKey")   apiKey: String
    ): NewsResponse
}
