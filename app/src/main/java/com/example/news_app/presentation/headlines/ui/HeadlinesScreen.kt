package com.example.news_app.presentation.headlines.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.news_app.BuildConfig
import com.example.news_app.R
import com.example.news_app.presentation.headlines.viewmodel.HeadlinesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HeadlinesScreen(
    onOpenArticle: (String) -> Unit,
    viewModel: HeadlinesViewModel = hiltViewModel()
) {
    val items = viewModel.headlines.collectAsLazyPagingItems()
    val uiState = viewModel.uiState.collectAsState().value

    LaunchedEffect(items.loadState, items.itemCount) {
        viewModel.onPagingStateChanged(
            loadStates = items.loadState,
            itemCount = items.itemCount
        )
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text(BuildConfig.NEWS_SOURCE_NAME) }) }
    ) { padding ->

        when (val mode = uiState.mode) {
            is HeadlinesUiState.Mode.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }
            }

            is HeadlinesUiState.Mode.Error -> {
                HeadlinesErrorState(
                    message = stringResource(mode.messageRes),
                    onRetry = { items.retry() }
                )
            }

            is HeadlinesUiState.Mode.Empty -> {
                HeadlinesEmptyState()
            }

            is HeadlinesUiState.Mode.Content -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    uiState.banner?.let { banner ->
                        item(key = "banner") {
                            HeadlinesInlineErrorBanner(
                                message = stringResource(banner.messageRes),
                                onRetry = { items.retry() }
                            )
                        }
                    }

                    items(count = items.itemCount) { index ->
                        val article = items[index] ?: return@items
                        HeadlineRow(
                            title = article.title,
                            imageUrl = article.urlToImage,
                            publishedAtIso = article.publishedAtIso,
                            onClick = { onOpenArticle(article.id) }
                        )
                    }

                    when (items.loadState.append) {
                        is LoadState.Loading -> item {
                            Box(
                                Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) { CircularProgressIndicator() }
                        }
                        is LoadState.Error -> item {
                            HeadlinesErrorState(
                                message = stringResource(R.string.failed_loading_more),
                                onRetry = { items.retry() }
                            )
                        }
                        else -> Unit
                    }
                }
            }
        }
    }
}