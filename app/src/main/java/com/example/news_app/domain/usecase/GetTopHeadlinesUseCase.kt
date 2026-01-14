package com.example.news_app.domain.usecase

import androidx.paging.PagingData
import com.example.news_app.domain.Article
import com.example.news_app.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTopHeadlinesUseCase @Inject constructor(
    private val repo: NewsRepository
) {
    operator fun invoke(): Flow<PagingData<Article>> = repo.topHeadlinesPaged()
}