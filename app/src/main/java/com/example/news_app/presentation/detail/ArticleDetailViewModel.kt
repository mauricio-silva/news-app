package com.example.news_app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.news_app.domain.usecase.GetArticleByIdUseCase
import com.example.news_app.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getArticleById: GetArticleByIdUseCase
) : ViewModel() {

    private val id: String = checkNotNull(savedStateHandle[Routes.DETAIL_ARG_ID])

    val uiState: StateFlow<ArticleDetailUiState> =
        getArticleById(id)
            .map { article ->
                if (article == null) ArticleDetailUiState.NotFound
                else ArticleDetailUiState.Data(article)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = ArticleDetailUiState.Loading
            )
}