package com.example.news_app.data.remote

import com.example.news_app.data.remote.dto.TopHeadlinesResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsApiService {

    @GET("v2/top-headlines")
    suspend fun topHeadlines(
        @Query("sources") sources: String,
        @Query("page") page: Int,
        @Query("pageSize") pageSize: Int
    ): TopHeadlinesResponseDto
}