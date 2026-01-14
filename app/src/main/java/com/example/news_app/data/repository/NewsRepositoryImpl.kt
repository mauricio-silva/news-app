package com.example.news_app.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.example.news_app.data.local.NewsDatabase
import com.example.news_app.data.mapper.toDomain
import com.example.news_app.data.paging.TopHeadlinesRemoteMediator
import com.example.news_app.data.remote.NewsApiService
import com.example.news_app.domain.Article
import com.example.news_app.domain.repository.NewsRepository
import com.yourcompany.ctwnews.BuildConfig
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepositoryImpl @Inject constructor(
    private val api: NewsApiService,
    private val db: NewsDatabase
) : NewsRepository {

    private val sourceId = BuildConfig.NEWS_SOURCE_ID

    @OptIn(ExperimentalPagingApi::class)
    override fun topHeadlinesPaged(): Flow<PagingData<Article>> {
        return Pager(
            config = PagingConfig(pageSize = 20, prefetchDistance = 5, initialLoadSize = 40),
            remoteMediator = TopHeadlinesRemoteMediator(sourceId, api, db),
            pagingSourceFactory = { db.articleDao().pagingSource(sourceId) }
        ).flow.map { pagingData ->
            pagingData.map { it.toDomain() }
        }
    }

    override fun articleById(id: String): Flow<Article?> =
        db.articleDao().observeById(id).map { it?.toDomain() }
}
