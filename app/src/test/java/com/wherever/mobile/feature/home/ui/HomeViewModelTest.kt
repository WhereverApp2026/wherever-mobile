package com.wherever.mobile.feature.home.ui

import app.cash.turbine.test
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/** Inicio provisional (spec nv-17): lee el perfil de Room, sin red propia. */
class HomeViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    @Test
    fun `sin perfil en cache muestra vacio y luego el nombre que llega`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeProfileRepository()
        val viewModel = HomeViewModel(repository)

        viewModel.profile.test {
            assertNull(awaitItem())
            repository.profile.value = sampleProfile(name = "Ana López")
            assertEquals("Ana López", awaitItem()?.name)
        }
    }

    @Test
    fun `con perfil en cache lo muestra de inmediato`() = runTest(mainDispatcher.dispatcher) {
        val repository = FakeProfileRepository(sampleProfile(name = "Ana"))
        val viewModel = HomeViewModel(repository)

        viewModel.profile.test {
            val first = awaitItem() ?: awaitItem()
            assertEquals("Ana", first?.name)
        }
    }
}
