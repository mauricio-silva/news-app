package com.example.news_app.data

import com.example.news_app.data.remote.NewsApiKeyInterceptor
import com.example.news_app.data.remote.NewsApiService
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

class NewsApiServiceMockWebServerTest {

    private lateinit var server: MockWebServer
    private lateinit var api: NewsApiService

    @BeforeEach
    fun setUp() {
        server = MockWebServer()
        server.start()

        val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            explicitNulls = false
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(NewsApiKeyInterceptor("test-api-key"))
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

        api = retrofit.create(NewsApiService::class.java)
    }

    @AfterEach
    fun tearDown() {
        server.shutdown()
    }

    @Test
    fun `topHeadlines sends expected path and X-Api-Key header`() = runTest {
        server.enqueue(
            MockResponse()
                .setResponseCode(200)
                .setBody("""{"status":"ok","totalResults":0,"articles":[]}""")
        )

        api.topHeadlines(
            sources = "bbc-news",
            page = 1,
            pageSize = 20
        )

        val req = server.takeRequest()
        assertEquals("GET", req.method)
        assertEquals("/v2/top-headlines?sources=bbc-news&page=1&pageSize=20", req.path)
        assertEquals("test-api-key", req.getHeader("X-Api-Key"))
    }
}