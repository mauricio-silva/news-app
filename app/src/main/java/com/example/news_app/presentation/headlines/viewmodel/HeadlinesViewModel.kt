package com.example.news_app.presentation.headlines.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import com.example.news_app.domain.Article
import com.example.news_app.domain.usecase.GetTopHeadlinesUseCase
import com.example.news_app.presentation.headlines.mapper.HeadlinesErrorMessageMapper
import com.example.news_app.presentation.headlines.ui.HeadlinesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class HeadlinesViewModel @Inject constructor(
    getTopHeadlines: GetTopHeadlinesUseCase,
    private val errorMapper: HeadlinesErrorMessageMapper
) : ViewModel() {

    val headlines: Flow<PagingData<Article>> = getTopHeadlines().cachedIn(viewModelScope)

    private val _uiState = MutableStateFlow(HeadlinesUiState())
    val uiState: StateFlow<HeadlinesUiState> = _uiState.asStateFlow()

    fun onPagingStateChanged(loadStates: CombinedLoadStates, itemCount: Int) {
        val refresh = loadStates.refresh
        val append = loadStates.append
        val hasItems = itemCount > 0

        val next = when {
            !hasItems && refresh is LoadState.Loading ->
                HeadlinesUiState(mode = HeadlinesUiState.Mode.Loading)

            !hasItems && refresh is LoadState.Error ->
                HeadlinesUiState(
                    mode = HeadlinesUiState.Mode.Error(messageRes = errorMapper.toMessageRes(refresh.error))
                )

            !hasItems && refresh is LoadState.NotLoading ->
                HeadlinesUiState(mode = HeadlinesUiState.Mode.Empty)

            else -> {
                val refreshBanner = (refresh as? LoadState.Error)
                    ?.let { HeadlinesUiState.Banner(messageRes = errorMapper.toMessageRes(it.error)) }

                val appendBanner = (append as? LoadState.Error)
                    ?.let { HeadlinesUiState.Banner(messageRes = errorMapper.toMessageRes(it.error)) }

                HeadlinesUiState(
                    mode = HeadlinesUiState.Mode.Content,
                    banner = refreshBanner ?: appendBanner
                )
            }
        }

        if (_uiState.value != next) _uiState.value = next
    }
}
