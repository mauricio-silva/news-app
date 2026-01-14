package com.example.news_app.data.dao

import androidx.room.Room
import com.example.news_app.data.local.NewsDatabase
import com.example.news_app.data.local.dao.RemoteKeysDao
import com.example.news_app.data.local.entity.RemoteKeysEntity
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class RemoteKeysDaoTest {

    private lateinit var db: NewsDatabase
    private lateinit var dao: RemoteKeysDao

    @Before
    fun setUp() {
        val context = RuntimeEnvironment.getApplication()
        db = Room.inMemoryDatabaseBuilder(context, NewsDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        dao = db.remoteKeysDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `insertAll and remoteKeys returns expected keys`() = runTest {
        val key = RemoteKeysEntity(
            articleId = "a1",
            sourceId = "bbc-news",
            prevKey = null,
            nextKey = 2
        )

        dao.insertAll(listOf(key))

        val loaded = dao.remoteKeys("a1", "bbc-news")
        assertNotNull(loaded)
        assertEquals(null, loaded?.prevKey)
        assertEquals(2, loaded?.nextKey)
    }

    @Test
    fun `clearBySource removes only that source`() = runTest {
        val bbc = RemoteKeysEntity("a1", "bbc-news", prevKey = null, nextKey = 2)
        val cnn = RemoteKeysEntity("a2", "cnn", prevKey = null, nextKey = 2)

        dao.insertAll(listOf(bbc, cnn))
        dao.clearBySource("bbc-news")

        assertNull(dao.remoteKeys("a1", "bbc-news"))
        assertNotNull(dao.remoteKeys("a2", "cnn"))
    }
}