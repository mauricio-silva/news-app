package com.example.news_app.domain

import app.cash.turbine.test
import com.example.news_app.MainDispatcherExtension
import com.example.news_app.domain.usecase.GetArticleByIdUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MainDispatcherExtension::class)
class GetArticleByIdUseCaseTest {

    @Test
    fun `use case emits article`() = runTest {
        val article = Article(
            id = "id1",
            sourceName = "BBC",
            author = null,
            title = "Title",
            description = "Desc",
            url = "https://x",
            urlToImage = null,
            publishedAtIso = "2026-01-12T00:00:00Z",
            content = "Content"
        )
        val useCase = mockk<GetArticleByIdUseCase>()
        every { useCase.invoke("id1") } returns flowOf(article)

        useCase("id1").test {
            Assertions.assertEquals(article, awaitItem())
            awaitComplete()
        }
    }
}