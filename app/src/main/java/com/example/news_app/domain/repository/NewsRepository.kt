package com.example.news_app.domain.repository

import androidx.paging.PagingData
import com.example.news_app.domain.Article
import kotlinx.coroutines.flow.Flow

interface NewsRepository {
    fun topHeadlinesPaged(): Flow<PagingData<Article>>
    fun articleById(id: String): Flow<Article?>
}
