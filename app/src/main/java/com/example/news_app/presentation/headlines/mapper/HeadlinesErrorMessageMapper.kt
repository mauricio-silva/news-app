package com.example.news_app.presentation.headlines.mapper

import androidx.annotation.StringRes

fun interface HeadlinesErrorMessageMapper {
    @StringRes
    fun toMessageRes(t: Throwable): Int
}