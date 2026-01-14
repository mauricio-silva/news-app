package com.example.news_app.presentation

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.example.news_app.domain.Article
import com.example.news_app.domain.usecase.GetArticleByIdUseCase
import com.example.news_app.presentation.detail.ArticleDetailUiState
import com.example.news_app.presentation.detail.ArticleDetailViewModel
import com.example.news_app.presentation.navigation.Routes
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ArticleDetailViewModelTest {

    @Test
    fun `uiState starts as Loading`() = runTest {
        val flow = MutableSharedFlow<Article?>(replay = 0)
        val useCase = mockk<GetArticleByIdUseCase>()
        every { useCase.invoke("id1") } returns flow

        val savedState = SavedStateHandle(mapOf(Routes.DETAIL_ARG_ID to "id1"))
        val vm = ArticleDetailViewModel(savedState, useCase)

        vm.uiState.test {
            val first = awaitItem()
            assertTrue(first is ArticleDetailUiState.Loading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when useCase emits null then uiState becomes NotFound`() = runTest {
        val upstream = kotlinx.coroutines.flow.MutableStateFlow<Article?>(null)

        val useCase = mockk<GetArticleByIdUseCase>()
        every { useCase.invoke("id1") } returns upstream

        val vm = ArticleDetailViewModel(
            SavedStateHandle(mapOf(Routes.DETAIL_ARG_ID to "id1")),
            useCase
        )

        vm.uiState.test {
            assertTrue(awaitItem() is ArticleDetailUiState.Loading)

            val next = awaitItem()
            assertTrue(next is ArticleDetailUiState.NotFound)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when useCase emits article then uiState becomes Data`() = runTest {
        val article = Article(
            id = "a1",
            sourceName = "BBC News",
            author = null,
            title = "Title",
            description = "Desc",
            url = "https://example.com/1",
            urlToImage = "https://img/1.jpg",
            publishedAtIso = "2026-01-12T21:24:00Z",
            content = "Content"
        )

        val upstream = kotlinx.coroutines.flow.MutableStateFlow<Article?>(article)

        val useCase = mockk<GetArticleByIdUseCase>()
        every { useCase.invoke("id1") } returns upstream

        val vm = ArticleDetailViewModel(
            SavedStateHandle(mapOf(Routes.DETAIL_ARG_ID to "id1")),
            useCase
        )

        vm.uiState.test {
            // initial from stateIn
            assertTrue(awaitItem() is ArticleDetailUiState.Loading)

            // next should be Data because upstream is already article
            val next = awaitItem()
            assertTrue(next is ArticleDetailUiState.Data)

            val data = next as ArticleDetailUiState.Data
            assertEquals(article, data.article)

            cancelAndIgnoreRemainingEvents()
        }
    }
}