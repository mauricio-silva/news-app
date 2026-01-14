package com.example.news_app.presentation.headlines.ui

import androidx.annotation.StringRes
import com.example.news_app.R

data class HeadlinesUiState(
    val mode: Mode = Mode.Content,
    val banner: Banner? = null
) {
    sealed interface Mode {
        data object Loading : Mode
        data object Content : Mode
        data object Empty : Mode
        data class Error(
            @StringRes val messageRes: Int = R.string.unexpected_error
        ) : Mode
    }

    data class Banner(
        @StringRes val messageRes: Int,
        val kind: Kind = Kind.Warning
    ) {
        enum class Kind { Info, Warning, Error }
    }
}