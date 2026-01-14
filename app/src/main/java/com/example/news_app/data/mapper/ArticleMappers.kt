package com.example.news_app.data.mapper

import com.example.news_app.data.local.entity.ArticleEntity
import com.example.news_app.data.remote.dto.ArticleDto
import com.example.news_app.domain.Article
import java.security.MessageDigest

internal fun ArticleDto.toEntity(sourceId: String): ArticleEntity? {
    val url = url?.trim().orEmpty()
    val title = title?.trim().orEmpty()
    val published = publishedAt?.trim().orEmpty()
    if (url.isBlank() || title.isBlank() || published.isBlank()) return null

    val id = sha256(url)

    return ArticleEntity(
        id = id,
        sourceId = sourceId,
        sourceName = source?.name.orEmpty(),
        author = author,
        title = title,
        description = description,
        url = url,
        urlToImage = urlToImage,
        publishedAtIso = published,
        content = content
    )
}

internal fun ArticleEntity.toDomain(): Article = Article(
    id = id,
    sourceName = sourceName,
    author = author,
    title = title,
    description = description,
    url = url,
    urlToImage = urlToImage,
    publishedAtIso = publishedAtIso,
    content = content
)

private fun sha256(input: String): String {
    val md = MessageDigest.getInstance("SHA-256")
    val bytes = md.digest(input.toByteArray())
    return bytes.joinToString("") { "%02x".format(it) }
}
