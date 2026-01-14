package com.example.news_app.domain.usecase

import com.example.news_app.domain.Article
import com.example.news_app.domain.repository.NewsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetArticleByIdUseCase @Inject constructor(
    private val repo: NewsRepository
) {
    operator fun invoke(id: String): Flow<Article?> = repo.articleById(id)
}