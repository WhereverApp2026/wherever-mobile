package com.wherever.mobile.integration

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.core.security.SessionTokens
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.profile.data.ProfileRepositoryImpl
import com.wherever.mobile.feature.profile.data.UsersApi
import com.wherever.mobile.feature.tastes.ui.TastesViewModel
import com.wherever.mobile.testing.ApiDoc
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.FakeUserProfileDao
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.RealNetwork
import com.wherever.mobile.testing.reply
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mockwebserver3.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.Timeout

/**
 * Integración nv-44: `TastesViewModel` + `ProfileRepositoryImpl` + cadena de red real contra MockWebServer.
 * El presupuesto escrito a mano llega como enteros a `PUT /usuarios/yo/gustos`, y un 422 en un campo
 * del presupuesto se muestra bajo ese campo.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TastesBudgetIntegrationTest {
    @get:Rule
    val timeout: Timeout = Timeout.seconds(20)

    @get:Rule
    val mainRule = MainDispatcherRule()

    private val server = MockWebServer()
    private val tokenStore = FakeTokenStore(SessionTokens("acceso", "refresh", "2026-11-04T15:15:00Z"))
    private lateinit var repository: ProfileRepositoryImpl

    @Before
    fun setUp() {
        server.start()
        repository = ProfileRepositoryImpl(
            RealNetwork(server, tokenStore).retrofit.create(UsersApi::class.java),
            FakeUserProfileDao()
        )
    }

    @After
    fun tearDown() = server.close()

    private fun TestScope.filledViewModel(): TastesViewModel {
        val viewModel = TastesViewModel(SavedStateHandle(), repository, FakeAuthRepository())
        runCurrent()
        listOf("cafes", "museos", "parques").forEach(viewModel::toggleCategory)
        viewModel.onBudgetMinChange("0120")
        viewModel.onBudgetMaxChange("1500")
        return viewModel
    }

    @Test
    fun `200 manda presupuesto_min y presupuesto_max enteros`() = runTest {
        server.reply(200, ApiDoc.response("PUT /api/v1/usuarios/yo/gustos", 200))
        server.reply(200, ApiDoc.response("GET /api/v1/usuarios/yo", 200), "ETag" to "\"3\"")
        val viewModel = filledViewModel()

        viewModel.finished.test {
            viewModel.submit()
            awaitItem()
        }

        val request = server.takeRequest()
        assertEquals("PUT", request.method)
        assertEquals("/api/v1/usuarios/yo/gustos", request.url.encodedPath)
        val body = Json.parseToJsonElement(request.body!!.utf8()).jsonObject
        assertEquals(120, body.getValue("presupuesto_min").jsonPrimitive.int)
        assertEquals(1500, body.getValue("presupuesto_max").jsonPrimitive.int)
        assertEquals(false, body.getValue("presupuesto_min").jsonPrimitive.isString)
    }

    @Test
    fun `422 en presupuesto_max muestra el mensaje bajo el campo`() = runTest {
        server.reply(
            422,
            """{"codigo":"validacion","mensaje":"datos inválidos","campos":[{"campo":"presupuesto_max",""" +
                """"mensaje":"debe ser mayor o igual que presupuesto_min"}],"traza_id":"t1"}"""
        )
        val viewModel = filledViewModel()

        viewModel.submit()

        // La respuesta llega por el hilo de OkHttp: se espera al estado final, no al reloj virtual.
        val state = viewModel.state.first { it.budgetMaxError != null }
        assertEquals(UiText.Plain("Debe ser mayor o igual que presupuesto_min"), state.budgetMaxError)
        assertNull(state.budgetMinError)
        assertNull(state.error)
        assertEquals(false, state.saving)
    }
}
