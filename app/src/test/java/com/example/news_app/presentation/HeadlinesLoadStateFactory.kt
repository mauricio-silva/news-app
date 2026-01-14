package com.example.news_app.presentation

import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import androidx.paging.LoadStates

internal fun combinedLoadStates(
    refresh: LoadState,
    append: LoadState = LoadState.NotLoading(endOfPaginationReached = false),
    prepend: LoadState = LoadState.NotLoading(endOfPaginationReached = true)
): CombinedLoadStates {
    val source = LoadStates(
        refresh = refresh,
        prepend = prepend,
        append = append
    )

    return CombinedLoadStates(
        refresh = refresh,
        prepend = prepend,
        append = append,
        source = source,
        mediator = null
    )
}
