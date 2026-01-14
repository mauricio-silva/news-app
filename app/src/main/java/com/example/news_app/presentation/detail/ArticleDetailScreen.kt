package com.example.news_app.presentation.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil3.compose.AsyncImage
import com.example.news_app.R
import com.example.news_app.domain.Article
import com.example.news_app.presentation.util.DateFormatter
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailScreen(
    onBack: () -> Unit,
    vm: ArticleDetailViewModel = hiltViewModel()
) {
    var articleDetailUi by remember { mutableStateOf<DetailUi>(DetailUi.Loading) }

    LaunchedEffect(Unit) {
        vm.article.collectLatest { article ->
            articleDetailUi = if (article == null) DetailUi.NotFound else DetailUi.Data(article)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        when (val articleDetail = articleDetailUi) {
            DetailUi.Loading -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { CircularProgressIndicator() }

            DetailUi.NotFound -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) { Text(stringResource(R.string.article_not_found_message)) }

            is DetailUi.Data -> {
                val ui = articleDetail.article
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = ui.urlToImage,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(ui.title, style = MaterialTheme.typography.headlineSmall)
                    Text(
                        text = "Published: ${DateFormatter.formatIsoToDisplay(ui.publishedAtIso)}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    if (!ui.description.isNullOrBlank()) Text(
                        ui.description,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (!ui.content.isNullOrBlank()) Text(
                        ui.content,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

private sealed interface DetailUi {
    data object Loading : DetailUi
    data object NotFound : DetailUi
    data class Data(val article: Article) : DetailUi
}