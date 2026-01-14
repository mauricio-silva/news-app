package com.example.news_app.di


import android.content.Context
import androidx.room.Room
import com.example.news_app.data.local.NewsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val DATABASE_NAME = "news.db"

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideDb(@ApplicationContext ctx: Context): NewsDatabase =
        Room.databaseBuilder(ctx, NewsDatabase::class.java, DATABASE_NAME)
            .fallbackToDestructiveMigration()
            .build()
}
