package com.example.news_app.data.remote

class NewsApiException(
    val httpStatus: Int,
    val apiCode: String?,
    override val message: String
) : RuntimeException(message)
