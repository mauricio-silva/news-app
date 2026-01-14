package com.example.news_app.presentation.headlines.mapper

import androidx.annotation.StringRes
import com.example.news_app.R
import com.example.news_app.data.remote.NewsApiException
import java.io.IOException

class HeadlinesErrorMapper : HeadlinesErrorMessageMapper {

    @StringRes
    override fun toMessageRes(t: Throwable): Int {
        return when (t) {
            is IOException -> R.string.no_internet_check_your_connectivity

            is NewsApiException -> when (t.apiCode) {
                "rateLimited" -> R.string.error_rate_limited
                "apiKeyInvalid", "apiKeyMissing", "apiKeyDisabled", "apiKeyExhausted" -> R.string.error_authentication
                "parametersMissing", "parameterInvalid" -> R.string.error_bad_request
                "sourceDoesNotExist" -> R.string.error_source_not_found
                "unexpectedError" -> R.string.unexpected_error
                else -> R.string.unexpected_error
            }

            else -> R.string.unexpected_error
        }
    }
}