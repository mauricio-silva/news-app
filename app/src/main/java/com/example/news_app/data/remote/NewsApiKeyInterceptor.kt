package com.example.news_app.data.remote

import okhttp3.Interceptor
import okhttp3.Response

private const val X_API_KEY = "X-Api-Key"

class NewsApiKeyInterceptor(
    private val apiKey: String
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request().newBuilder()
            .header(X_API_KEY, apiKey)
            .build()
        return chain.proceed(req)
    }
}
