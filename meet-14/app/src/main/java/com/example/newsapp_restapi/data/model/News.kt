package com.example.newsapp_restapi.data.model

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("status")       val status: String,
    @SerializedName("totalResults") val totalResults: Int,
    @SerializedName("articles")     val articles: List<Article>
)

data class Article(
    @SerializedName("source")      val source: Source?,
    @SerializedName("author")      val author: String?,
    @SerializedName("title")       val title: String?,
    @SerializedName("description") val description: String?,
    @SerializedName("url")         val url: String?,
    @SerializedName("urlToImage")  val urlToImage: String?,
    @SerializedName("publishedAt") val publishedAt: String?,
    @SerializedName("content")     val content: String?
) {
    val safeTitle: String   get() = title ?: "No Title"
    val safeDescription: String get() = description ?: "No description available."
    val safeSource: String  get() = source?.name ?: "Unknown Source"
    val safeAuthor: String  get() = if (!author.isNullOrBlank()) author else "Unknown"
    val safeContent: String get() = content?.replace(Regex("""\[\+\d+ chars\]"""), "...") ?: safeDescription
}

data class Source(
    @SerializedName("id")   val id: String?,
    @SerializedName("name") val name: String?
)