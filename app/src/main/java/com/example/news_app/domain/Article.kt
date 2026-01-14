package com.example.news_app.domain

data class Article(
    val id: String,
    val sourceName: String,
    val author: String?,
    val title: String,
    val description: String?,
    val url: String,
    val urlToImage: String?,
    val publishedAtIso: String,
    val content: String?
)
