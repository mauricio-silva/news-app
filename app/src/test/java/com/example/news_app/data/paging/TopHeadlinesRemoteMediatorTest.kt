package com.example.news_app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.Room
import com.example.news_app.data.local.NewsDatabase
import com.example.news_app.data.local.dao.ArticleDao
import com.example.news_app.data.local.entity.ArticleEntity
import com.example.news_app.data.remote.NewsApiKeyInterceptor
import com.example.news_app.data.remote.NewsApiService
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

@OptIn(ExperimentalPagingApi::class)
@RunWith(RobolectricTestRunner::class)
class TopHeadlinesRemoteMediatorTest {

    private lateinit var server: MockWebServer
    private lateinit var db: NewsDatabase
    private lateinit var api: NewsApiService
    private lateinit var json: Json

    private val sourceId = "bbc-news"
    private val apiKey = "test-api-key"

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()

        db = Room.inMemoryDatabaseBuilder(
            RuntimeEnvironment.getApplication(),
            NewsDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(NewsApiKeyInterceptor(apiKey))
            .build()

        api = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NewsApiService::class.java)
    }

    @After
    fun tearDown() {
        db.close()
        server.shutdown()
    }

    @Test
    fun `REFRESH then APPEND - validates request paths, header, and persistence`() = runTest {
        server.enqueue(MockResponse().setResponseCode(200).setBody(page1Json()))
        server.enqueue(MockResponse().setResponseCode(200).setBody(page2Json()))

        val mediator = TopHeadlinesRemoteMediator(
            sourceId = sourceId,
            api = api,
            db = db,
            json = json
        )

        val refreshState = emptyPagingState<ArticleEntity>(pageSize = 20)
        val refreshResult = mediator.load(LoadType.REFRESH, refreshState)
        assertTrue(refreshResult is RemoteMediator.MediatorResult.Success)

        val req1 = server.takeRequest()
        assertEquals("/v2/top-headlines?sources=bbc-news&page=1&pageSize=20", req1.path)
        assertEquals(apiKey, req1.getHeader("X-Api-Key"))

        val afterRefresh = db.articleDao().pagingSource(sourceId).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false)
        )
        val refreshPage = afterRefresh as PagingSource.LoadResult.Page
        assertEquals(2, refreshPage.data.size)

        val last = refreshPage.data.last()
        val appendState = pagingStateWithSinglePage(
            data = listOf(last),
            pageSize = 20
        )

        val appendResult = mediator.load(LoadType.APPEND, appendState)
        assertTrue(appendResult is RemoteMediator.MediatorResult.Success)

        val req2 = server.takeRequest()
        assertEquals("/v2/top-headlines?sources=bbc-news&page=2&pageSize=20", req2.path)
        assertEquals(apiKey, req2.getHeader("X-Api-Key"))

        val afterAppend = db.articleDao().pagingSource(sourceId).load(
            PagingSource.LoadParams.Refresh(key = null, loadSize = 50, placeholdersEnabled = false)
        ) as PagingSource.LoadResult.Page

        assertEquals(3, afterAppend.data.size)

        val keys = db.remoteKeysDao().remoteKeys(articleId = last.id, sourceId = sourceId)
        assertNotNull(keys)
        assertEquals(2, keys?.nextKey) // from refresh load page=1 => nextKey=2
    }

    @Test
    fun `REFRESH returns Error when API responds 401 with error body`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(401)
                .setBody("""{"status":"error","code":"apiKeyInvalid","message":"Your API key is invalid."}""")
        )

        val mediator = TopHeadlinesRemoteMediator(sourceId, api, db, json)
        val result = mediator.load(LoadType.REFRESH, emptyPagingState(pageSize = 20))

        assertTrue(result is RemoteMediator.MediatorResult.Error)
        val err = (result as RemoteMediator.MediatorResult.Error).throwable
        assertTrue(err.message?.contains("invalid", ignoreCase = true) == true)
    }

    @Test
    fun `initialize skips refresh when cache exists`() = runTest {
        val db = mockk<NewsDatabase>()
        val articleDao = mockk<ArticleDao>()
        every { db.articleDao() } returns articleDao
        coEvery { articleDao.countBySource("bbc-news") } returns 10

        val mediator = TopHeadlinesRemoteMediator(
            sourceId = "bbc-news",
            api = mockk(),
            db = db,
            json = Json { ignoreUnknownKeys = true }
        )

        val action = mediator.initialize()
        assertEquals(RemoteMediator.InitializeAction.SKIP_INITIAL_REFRESH, action)
    }

    @Test
    fun `initialize launches refresh when cache is empty`() = runTest {
        val db = mockk<NewsDatabase>()
        val articleDao = mockk<ArticleDao>()
        every { db.articleDao() } returns articleDao
        coEvery { articleDao.countBySource("bbc-news") } returns 0

        val mediator = TopHeadlinesRemoteMediator(
            sourceId = "bbc-news",
            api = mockk(),
            db = db,
            json = Json { ignoreUnknownKeys = true }
        )

        val action = mediator.initialize()
        assertEquals(RemoteMediator.InitializeAction.LAUNCH_INITIAL_REFRESH, action)
    }

    private fun page1Json(): String = """
        {
          "status":"ok",
          "totalResults":3,
          "articles":[
            {
              "source":{"id":null,"name":"BBC News"},
              "author":"A",
              "title":"Title 1",
              "description":"Desc 1",
              "url":"https://example.com/1",
              "urlToImage":"https://img/1.jpg",
              "publishedAt":"2026-01-12T10:00:00Z",
              "content":"C1"
            },
            {
              "source":{"id":null,"name":"BBC News"},
              "author":"B",
              "title":"Title 2",
              "description":"Desc 2",
              "url":"https://example.com/2",
              "urlToImage":"https://img/2.jpg",
              "publishedAt":"2026-01-12T12:00:00Z",
              "content":"C2"
            }
          ]
        }
    """.trimIndent()

    private fun page2Json(): String = """
        {
          "status":"ok",
          "totalResults":3,
          "articles":[
            {
              "source":{"id":null,"name":"BBC News"},
              "author":"C",
              "title":"Title 3",
              "description":"Desc 3",
              "url":"https://example.com/3",
              "urlToImage":"https://img/3.jpg",
              "publishedAt":"2026-01-12T13:00:00Z",
              "content":"C3"
            }
          ]
        }
    """.trimIndent()

    private fun <T : Any> emptyPagingState(pageSize: Int): PagingState<Int, T> {
        return PagingState(
            pages = emptyList(),
            anchorPosition = null,
            config = PagingConfig(pageSize = pageSize),
            leadingPlaceholderCount = 0
        )
    }

    private fun <T : Any> pagingStateWithSinglePage(
        data: List<T>,
        pageSize: Int
    ): PagingState<Int, T> {
        val page = PagingSource.LoadResult.Page<Int, T>(
            data = data,
            prevKey = null,
            nextKey = null
        )
        return PagingState(
            pages = listOf(page),
            anchorPosition = data.lastIndex,
            config = PagingConfig(pageSize = pageSize),
            leadingPlaceholderCount = 0
        )
    }
}