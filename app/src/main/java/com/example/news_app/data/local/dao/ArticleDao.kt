package com.example.news_app.data.local.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.news_app.data.local.entity.ArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArticleDao {

    @Query("""
        SELECT * FROM articles
        WHERE sourceId = :sourceId
        ORDER BY publishedAtIso ASC
    """)
    fun pagingSource(sourceId: String): PagingSource<Int, ArticleEntity>

    @Query("SELECT * FROM articles WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<ArticleEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(items: List<ArticleEntity>)

    @Query("DELETE FROM articles WHERE sourceId = :sourceId")
    suspend fun clearBySource(sourceId: String)

    @Query("SELECT COUNT(*) FROM articles WHERE sourceId = :sourceId")
    suspend fun countBySource(sourceId: String): Int
}