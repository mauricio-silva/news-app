package com.example.news_app.di

import com.example.news_app.presentation.headlines.mapper.HeadlinesErrorMapper
import com.example.news_app.presentation.headlines.mapper.HeadlinesErrorMessageMapper
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PresentationModule {

    @Provides
    @Singleton
    fun provideHeadlinesErrorMessageMapper(): HeadlinesErrorMessageMapper =
        HeadlinesErrorMapper()
}