package com.example.news_app.data

import com.example.news_app.data.mapper.toEntity
import com.example.news_app.data.remote.dto.ArticleDto
import com.example.news_app.data.remote.dto.SourceDto
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.jupiter.api.Test

class ArticleMappersTest {

    @Test
    fun `dto missing url is rejected`() {
        val dto = ArticleDto(
            source = SourceDto(name = "BBC"),
            title = "Hello",
            url = null,
            publishedAt = "2026-01-12T00:00:00Z"
        )
        assertNull(dto.toEntity("bbc-news"))
    }

    @Test
    fun `valid dto maps to entity`() {
        val dto = ArticleDto(
            source = SourceDto(name = "BBC"),
            title = "Hello",
            url = "https://example.com",
            publishedAt = "2026-01-12T00:00:00Z"
        )
        assertNotNull(dto.toEntity("bbc-news"))
    }
}