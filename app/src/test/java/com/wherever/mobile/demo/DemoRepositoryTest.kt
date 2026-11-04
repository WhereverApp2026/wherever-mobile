package com.wherever.mobile.demo

import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.ui.UiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DemoRepositoryTest {

    @Test
    fun refreshPersistsRemoteEntitiesAndObservesMappedItems() = runTest {
        val repository = DemoRepository(TestDispatcherProvider(StandardTestDispatcher(testScheduler)))
        repository.remote = listOf(DemoEntity(1, "ana"))

        repository.refresh()
        val result = repository.observe().first()

        assertEquals(listOf(DemoItem(1, "ANA")), result.getOrNull())
    }

    @Test
    fun viewModelMovesFromLoadingToContent() = runTest {
        val repository = DemoRepository(TestDispatcherProvider(StandardTestDispatcher(testScheduler)))
        repository.remote = listOf(DemoEntity(1, "ana"))
        val viewModel = DemoViewModel(repository, this)

        viewModel.load()
        advanceUntilIdle()

        assertEquals(DemoItem(1, "ANA"), (viewModel.state.value as UiState.Content).data.single())
    }

    private class TestDispatcherProvider(private val dispatcher: CoroutineDispatcher) : DispatcherProvider {
        override val io: CoroutineDispatcher = dispatcher
        override val default: CoroutineDispatcher = dispatcher
        override val main: CoroutineDispatcher = dispatcher
    }
}
