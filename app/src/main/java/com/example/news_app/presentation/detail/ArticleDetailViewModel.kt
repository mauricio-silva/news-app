package com.example.news_app.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.news_app.domain.usecase.GetArticleByIdUseCase
import com.example.news_app.presentation.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ArticleDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    getArticleById: GetArticleByIdUseCase
) : ViewModel() {
    private val id: String = checkNotNull(savedStateHandle[Routes.DETAIL_ARG_ID])
    val article = getArticleById(id)
}