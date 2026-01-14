package com.example.news_app.data.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.RemoteMediator
import androidx.paging.PagingState
import androidx.room.withTransaction
import com.example.news_app.data.local.NewsDatabase
import com.example.news_app.data.local.entity.ArticleEntity
import com.example.news_app.data.local.entity.RemoteKeysEntity
import com.example.news_app.data.mapper.toEntity
import com.example.news_app.data.remote.NewsApiService
import retrofit2.HttpException
import java.io.IOException

@OptIn(ExperimentalPagingApi::class)
class TopHeadlinesRemoteMediator(
    private val sourceId: String,
    private val api: NewsApiService,
    private val db: NewsDatabase
) : RemoteMediator<Int, ArticleEntity>() {

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ArticleEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)

                val keys = db.remoteKeysDao().remoteKeys(lastItem.id, sourceId)
                keys?.nextKey ?: return MediatorResult.Success(endOfPaginationReached = true)
            }
        }

        try {
            val pageSize = state.config.pageSize.coerceAtMost(100)
            val response = api.topHeadlines(
                sources = sourceId,
                pageSize = pageSize,
                page = page
            )

            val entities = response.articles.mapNotNull { it.toEntity(sourceId) }
            val endOfPaginationReached = entities.isEmpty()

            db.withTransaction {
                if (loadType == LoadType.REFRESH) {
                    db.remoteKeysDao().clearBySource(sourceId)
                    db.articleDao().clearBySource(sourceId)
                }

                val prevKey = if (page == 1) null else page - 1
                val nextKey = if (endOfPaginationReached) null else page + 1

                db.remoteKeysDao().insertAll(
                    entities.map {
                        RemoteKeysEntity(
                            articleId = it.id,
                            sourceId = sourceId,
                            prevKey = prevKey,
                            nextKey = nextKey
                        )
                    }
                )
                db.articleDao().upsertAll(entities)
            }

            return MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        } catch (e: HttpException) {
            return MediatorResult.Error(e)
        }
    }
}
