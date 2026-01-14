package com.example.news_app.presentation.detail

import com.example.news_app.domain.Article

sealed interface ArticleDetailUiState {
    data object Loading : ArticleDetailUiState
    data object NotFound : ArticleDetailUiState
    data class Data(val article: Article) : ArticleDetailUiState
}