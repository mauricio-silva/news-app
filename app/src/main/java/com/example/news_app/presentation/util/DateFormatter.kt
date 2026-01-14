package com.example.news_app.presentation.util

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

object DateFormatter {
    private fun formatter(locale: Locale) =
        DateTimeFormatter.ofPattern("MMM dd, yyyy", locale)

    fun formatIsoToDisplay(
        isoUtc: String,
        locale: Locale = Locale.getDefault()
    ): String {
        return runCatching {
            val instant = Instant.parse(isoUtc)
            formatter(locale).format(instant.atZone(ZoneId.systemDefault()))
        }.getOrElse {
            isoUtc
        }
    }
}