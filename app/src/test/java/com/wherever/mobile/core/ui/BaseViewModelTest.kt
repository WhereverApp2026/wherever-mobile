package com.wherever.mobile.core.ui

import app.cash.turbine.test
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BaseViewModelTest {

    @Test
    fun exposesInitialState() = runTest {
        val viewModel = TestViewModel(this)

        assertEquals(0, viewModel.state.value)
    }

    @Test
    fun updatesStateWithSetAndUpdate() = runTest {
        val viewModel = TestViewModel(this)

        viewModel.replace(7)
        assertEquals(7, viewModel.state.value)

        viewModel.increment()
        assertEquals(8, viewModel.state.value)
    }

    @Test
    fun emitsEventsOnce() = runTest {
        val viewModel = TestViewModel(this)

        viewModel.events.test {
            viewModel.send("event")

            assertEquals("event", awaitItem())
            expectNoEvents()
        }
    }

    @Test
    fun emitsEventsInOrder() = runTest {
        val viewModel = TestViewModel(this)

        viewModel.events.test {
            viewModel.send("first")
            viewModel.send("second")

            assertEquals("first", awaitItem())
            assertEquals("second", awaitItem())
            expectNoEvents()
        }
    }

    private class TestViewModel(scope: CoroutineScope) : BaseViewModel<Int, String>(initialState = 0, injectedScope = scope) {

        fun replace(value: Int) = setState(value)

        fun increment() = updateState { it + 1 }

        fun send(event: String) = emitEvent(event)
    }
}
