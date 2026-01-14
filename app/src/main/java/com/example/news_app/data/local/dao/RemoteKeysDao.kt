package com.example.news_app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.news_app.data.local.entity.RemoteKeysEntity

@Dao
interface RemoteKeysDao {

    @Query("SELECT * FROM remote_keys WHERE articleId = :articleId AND sourceId = :sourceId LIMIT 1")
    suspend fun remoteKeys(articleId: String, sourceId: String): RemoteKeysEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(keys: List<RemoteKeysEntity>)

    @Query("DELETE FROM remote_keys WHERE sourceId = :sourceId")
    suspend fun clearBySource(sourceId: String)
}