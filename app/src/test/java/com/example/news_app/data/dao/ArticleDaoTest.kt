package com.example.news_app.data.dao

import androidx.room.Room
import com.example.news_app.data.local.NewsDatabase
import com.example.news_app.data.local.dao.ArticleDao
import com.example.news_app.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class ArticleDaoTest {

    private lateinit var db: NewsDatabase
    private lateinit var dao: ArticleDao

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.articleDao()
    }

    @After
    fun tearDown() {
        if (::db.isInitialized) db.close()
    }

    @Test
    fun `upsertAll inserts and observeById returns entity`() = runTest {
        val e = ArticleEntity(
            id = "a1",
            sourceId = "bbc-news",
            sourceName = "BBC News",
            author = "Author",
            title = "Title",
            description = "Desc",
            url = "https://example.com/1",
            urlToImage = "https://img/1.jpg",
            publishedAtIso = "2026-01-12T21:24:00Z",
            content = "Content"
        )

        dao.upsertAll(listOf(e))

        val observed = dao.observeById("a1").firstOrNull()
        assertNotNull(observed)
        assertEquals("a1", observed!!.id)
        assertEquals("bbc-news", observed.sourceId)
    }

    @Test
    fun `clearBySource removes only that source`() = runTest {
        val bbc = ArticleEntity(
            id = "a1",
            sourceId = "bbc-news",
            sourceName = "BBC News",
            author = null,
            title = "BBC 1",
            description = null,
            url = "https://bbc/1",
            urlToImage = null,
            publishedAtIso = "2026-01-12T21:24:00Z",
            content = null
        )
        val cnn = bbc.copy(id = "a2", sourceId = "cnn", title = "CNN 1", url = "https://cnn/1")

        dao.upsertAll(listOf(bbc, cnn))
        dao.clearBySource("bbc-news")

        assertNull(dao.observeById("a1").firstOrNull())
        assertNotNull(dao.observeById("a2").firstOrNull())
    }
}
