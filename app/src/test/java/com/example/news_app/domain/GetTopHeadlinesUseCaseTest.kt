package com.example.news_app.domain

import androidx.paging.PagingData
import com.example.news_app.domain.repository.NewsRepository
import com.example.news_app.domain.usecase.GetTopHeadlinesUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

class GetTopHeadlinesUseCaseTest {
    @Test
    fun `invokes repository paging flow`() {
        val repo = mockk<NewsRepository>()
        val expected = flowOf(PagingData.Companion.from(listOf<Article>()))
        every { repo.topHeadlinesPaged() } returns expected

        val sut = GetTopHeadlinesUseCase(repo)
        val actual = sut()

        Assertions.assertSame(expected, actual)
    }
}