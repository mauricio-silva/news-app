package com.example.news_app.presentation

import com.example.news_app.R
import androidx.paging.LoadState
import com.example.news_app.MainDispatcherExtension
import com.example.news_app.domain.usecase.GetTopHeadlinesUseCase
import com.example.news_app.presentation.headlines.mapper.HeadlinesErrorMessageMapper
import com.example.news_app.presentation.headlines.ui.HeadlinesUiState
import com.example.news_app.presentation.headlines.viewmodel.HeadlinesViewModel
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.extension.ExtendWith
import java.io.IOException

@ExtendWith(MainDispatcherExtension::class)
class HeadlinesViewModelTest {

    private fun vm(mapper: HeadlinesErrorMessageMapper): HeadlinesViewModel {
        val useCase = mockk<GetTopHeadlinesUseCase>()
        every { useCase.invoke() } returns emptyFlow()
        return HeadlinesViewModel(useCase, mapper)
    }

    @Test
    fun `no items + refresh loading to Loading mode`() = runTest {
        val sut = vm { R.string.unexpected_error }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(refresh = LoadState.Loading),
            itemCount = 0
        )

        assertTrue(sut.uiState.value.mode is HeadlinesUiState.Mode.Loading)
        assertNull(sut.uiState.value.banner)
    }

    @Test
    fun `no items + refresh error to Error mode with mapped message`() = runTest {
        val sut = vm { t ->
            if (t is IOException) R.string.no_internet_check_your_connectivity else R.string.unexpected_error
        }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(refresh = LoadState.Error(IOException())),
            itemCount = 0
        )

        val mode = sut.uiState.value.mode
        assertTrue(mode is HeadlinesUiState.Mode.Error)
        mode as HeadlinesUiState.Mode.Error
        assertEquals(R.string.no_internet_check_your_connectivity, mode.messageRes)
        assertNull(sut.uiState.value.banner)
    }

    @Test
    fun `no items + refresh not loading to Empty mode`() = runTest {
        val sut = vm { R.string.unexpected_error }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(refresh = LoadState.NotLoading(endOfPaginationReached = true)),
            itemCount = 0
        )

        assertTrue(sut.uiState.value.mode is HeadlinesUiState.Mode.Empty)
        assertNull(sut.uiState.value.banner)
    }

    @Test
    fun `has items + refresh not loading to Content without banner`() = runTest {
        val sut = vm { R.string.unexpected_error }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(refresh = LoadState.NotLoading(endOfPaginationReached = false)),
            itemCount = 10
        )

        assertTrue(sut.uiState.value.mode is HeadlinesUiState.Mode.Content)
        assertNull(sut.uiState.value.banner)
    }

    @Test
    fun `has items + refresh error to Content with banner mapped`() = runTest {
        val sut = vm { R.string.no_internet_check_your_connectivity }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(refresh = LoadState.Error(IOException())),
            itemCount = 10
        )

        assertTrue(sut.uiState.value.mode is HeadlinesUiState.Mode.Content)
        val banner = sut.uiState.value.banner
        assertNotNull(banner)
        assertEquals(R.string.no_internet_check_your_connectivity, banner.messageRes)
    }

    @Test
    fun `has items + refresh loading to stays Content`() = runTest {
        val sut = vm { R.string.unexpected_error }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(refresh = LoadState.Loading),
            itemCount = 3
        )

        assertTrue(sut.uiState.value.mode is HeadlinesUiState.Mode.Content)

        assertNull(sut.uiState.value.banner)
    }

    @Test
    fun `calling reducer with same inputs does not change state`() = runTest {
        val sut = vm { R.string.unexpected_error }

        val states = combinedLoadStates(refresh = LoadState.NotLoading(endOfPaginationReached = false))

        sut.onPagingStateChanged(states, itemCount = 5)
        val first = sut.uiState.value

        sut.onPagingStateChanged(states, itemCount = 5)
        val second = sut.uiState.value

        assertEquals(first, second)
    }

    @Test
    fun `has items + append error to Content with banner mapped`() = runTest {
        val sut = vm { R.string.no_internet_check_your_connectivity }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(
                refresh = LoadState.NotLoading(false),
                append = LoadState.Error(IOException())
            ),
            itemCount = 10
        )

        assertTrue(sut.uiState.value.mode is HeadlinesUiState.Mode.Content)
        assertNotNull(sut.uiState.value.banner)
        assertEquals(R.string.no_internet_check_your_connectivity, sut.uiState.value.banner!!.messageRes)
    }

    @Test
    fun `has items + refresh error and append error to refresh banner wins`() = runTest {
        val sut = vm { t ->
            if (t is IOException) R.string.no_internet_check_your_connectivity else R.string.unexpected_error
        }

        sut.onPagingStateChanged(
            loadStates = combinedLoadStates(
                refresh = LoadState.Error(IOException()),
                append = LoadState.Error(IllegalStateException("append"))
            ),
            itemCount = 10
        )

        assertEquals(R.string.no_internet_check_your_connectivity, sut.uiState.value.banner!!.messageRes)
    }
}