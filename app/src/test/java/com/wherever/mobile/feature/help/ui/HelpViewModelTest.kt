package com.wherever.mobile.feature.help.ui

import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.help.domain.Faq
import com.wherever.mobile.feature.help.domain.HelpRepository
import com.wherever.mobile.feature.help.domain.REPORT_MAX_LENGTH
import com.wherever.mobile.feature.help.domain.SupportContacts
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeHelpRepository : HelpRepository {
    var reportResult: Result<Unit> = Result.Success(Unit)
    val reports = mutableListOf<String>()

    override fun faqs(): List<Faq> = listOf(
        Faq(0, "¿Cómo cancelo una reservación?", "Entra a Perfil y abre Mis reservas."),
        Faq(1, "¿Qué es el Sello de Excelencia?", "Es una insignia de Wherever."),
        Faq(2, "¿Cómo uso un cupón?", "Toca Canjear y muestra el código QR.")
    )

    override fun contacts() = SupportContacts(email = "ayuda@ejemplo.com", whatsapp = null)

    override suspend fun reportProblem(message: String): Result<Unit> {
        reports += message
        return reportResult
    }
}

/** nv-33: filtro sin acentos, vacío, acordeón y envío del reporte (éxito, 422, sin red). */
@OptIn(ExperimentalCoroutinesApi::class)
class HelpViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeHelpRepository()
    private val viewModel = HelpViewModel(repository)

    @Test
    fun `empieza con todas las preguntas y los contactos del repositorio`() {
        val state = viewModel.state.value
        assertEquals(3, state.faqs.size)
        assertEquals("ayuda@ejemplo.com", state.contacts.email)
        assertFalse(state.noResults)
    }

    @Test
    fun `el filtro ignora acentos y mayusculas`() {
        viewModel.onQueryChange("RESERVACION")
        assertEquals(listOf(0), viewModel.state.value.faqs.map { it.id })

        viewModel.onQueryChange("  excelencia ")
        assertEquals(listOf(1), viewModel.state.value.faqs.map { it.id })
    }

    @Test
    fun `el filtro tambien busca en la respuesta`() {
        viewModel.onQueryChange("qr")
        assertEquals(listOf(2), viewModel.state.value.faqs.map { it.id })
    }

    @Test
    fun `sin coincidencias marca vacio y borrar la busqueda devuelve todas`() {
        viewModel.onQueryChange("mascotas")
        assertTrue(viewModel.state.value.faqs.isEmpty())
        assertTrue(viewModel.state.value.noResults)

        viewModel.onQueryChange("")
        assertEquals(3, viewModel.state.value.faqs.size)
        assertFalse(viewModel.state.value.noResults)
    }

    @Test
    fun `el acordeon abre una pregunta a la vez y se cierra al tocarla otra vez`() {
        viewModel.onToggleFaq(0)
        assertEquals(0, viewModel.state.value.expandedId)
        viewModel.onToggleFaq(1)
        assertEquals(1, viewModel.state.value.expandedId)
        viewModel.onToggleFaq(1)
        assertNull(viewModel.state.value.expandedId)
    }

    @Test
    fun `no se puede enviar un reporte vacio`() {
        viewModel.onOpenReport()
        viewModel.onReportChange("   ")
        assertFalse(viewModel.state.value.report!!.canSend)
        viewModel.sendReport()
        assertTrue(repository.reports.isEmpty())
    }

    @Test
    fun `lo escrito no pasa de 500 caracteres`() {
        viewModel.onOpenReport()
        viewModel.onReportChange("a".repeat(REPORT_MAX_LENGTH + 20))
        assertEquals(REPORT_MAX_LENGTH, viewModel.state.value.report!!.text.length)
        assertTrue(viewModel.state.value.report!!.canSend)
    }

    @Test
    fun `reporte enviado cierra la hoja y avisa`() = runTest {
        viewModel.onOpenReport()
        viewModel.onReportChange("El horario del lugar está mal")
        viewModel.reportSent.test {
            viewModel.sendReport()
            assertTrue(viewModel.state.value.report!!.sending)
            advanceUntilIdle()
            awaitItem()
        }
        assertNull(viewModel.state.value.report)
        assertEquals(listOf("El horario del lugar está mal"), repository.reports)
    }

    @Test
    fun `un 422 marca el campo y conserva lo escrito`() = runTest {
        repository.reportResult = Result.Failure(
            AppError.Validation(fields = listOf(FieldError("mensaje", "el mensaje es muy largo")))
        )
        viewModel.onOpenReport()
        viewModel.onReportChange("Algo falló")
        viewModel.sendReport()
        advanceUntilIdle()

        val report = viewModel.state.value.report!!
        assertEquals(UiText.Plain("El mensaje es muy largo"), report.fieldError)
        assertNull(report.error)
        assertEquals("Algo falló", report.text)
        assertFalse(report.sending)
    }

    @Test
    fun `sin red muestra el error y conserva lo escrito`() = runTest {
        repository.reportResult = Result.Failure(AppError.Network)
        viewModel.onOpenReport()
        viewModel.onReportChange("Algo falló")
        viewModel.sendReport()
        advanceUntilIdle()

        val report = viewModel.state.value.report!!
        assertNotNull(report.error)
        assertEquals("Algo falló", report.text)
        assertTrue(report.canSend)
    }

    @Test
    fun `sin sesion pide iniciar sesion en vez del error de la API`() = runTest {
        repository.reportResult = Result.Failure(AppError.Unauthorized("Autenticación requerida"))
        viewModel.onOpenReport()
        viewModel.onReportChange("Algo falló")
        viewModel.sendReport()
        advanceUntilIdle()

        val report = viewModel.state.value.report!!
        assertEquals(UiText.Res(R.string.help_report_sign_in), report.error)
        assertEquals("Algo falló", report.text)
    }

    @Test
    fun `escribir de nuevo borra el error anterior`() = runTest {
        repository.reportResult = Result.Failure(AppError.Network)
        viewModel.onOpenReport()
        viewModel.onReportChange("Algo falló")
        viewModel.sendReport()
        advanceUntilIdle()
        viewModel.onReportChange("Algo falló otra vez")
        assertNull(viewModel.state.value.report!!.error)
    }

    @Test
    fun `cerrar la hoja no la cierra mientras se envia`() = runTest {
        viewModel.onOpenReport()
        viewModel.onReportChange("Algo falló")
        viewModel.sendReport()
        viewModel.onDismissReport()
        assertNotNull(viewModel.state.value.report)
        advanceUntilIdle()
        viewModel.onOpenReport()
        viewModel.onDismissReport()
        assertNull(viewModel.state.value.report)
    }
}
