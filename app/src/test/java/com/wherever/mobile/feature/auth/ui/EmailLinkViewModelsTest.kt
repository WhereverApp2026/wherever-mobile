package com.wherever.mobile.feature.auth.ui

import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.auth.domain.EmailLink
import com.wherever.mobile.feature.auth.domain.PendingEmailLink
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EmailLinkViewModelsTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val auth = FakeAuthRepository()

    // Enlace

    @Test
    fun `lee el enlace https del correo, el esquema propio y el texto pegado`() {
        assertEquals(
            EmailLink.ResetPassword("abc_1-2"),
            EmailLink.parse("https://app.ejemplo.mx/restablecer#token=abc_1-2")
        )
        assertEquals(EmailLink.VerifyEmail("xyz"), EmailLink.parse("wherever://verificar#token=xyz"))
        assertEquals(
            EmailLink.ResetPassword("a b"),
            EmailLink.parse("  Toca aquí: https://app.ejemplo.mx/restablecer#token=a%20b  ")
        )
    }

    @Test
    fun `descarta enlaces sin token, de otra ruta o con caracteres raros`() {
        assertNull(EmailLink.parse(null))
        assertNull(EmailLink.parse("https://app.ejemplo.mx/restablecer"))
        assertNull(EmailLink.parse("https://app.ejemplo.mx/otra#token=abc"))
        assertNull(EmailLink.parse("wherever://restablecer#token=<script>"))
        assertNull(EmailLink.parse("wherever://restablecer#token=" + "a".repeat(600)))
    }

    // Nueva contraseña

    private fun holder(link: EmailLink?) = PendingEmailLink().apply { link?.let(::put) }

    private fun newPassword(token: String = "tok") =
        NewPasswordViewModel(holder(EmailLink.ResetPassword(token).takeIf { token.isNotEmpty() }), auth)

    @Test
    fun `nueva contrasena valida, manda el token y avisa a donde entrar`() = runTest {
        auth.resetResult = Result.Success(PostAuthDestination.TASTES)
        val viewModel = newPassword()
        viewModel.onPasswordChange("Nueva4567")
        viewModel.onConfirmChange("Nueva4567")
        assertTrue(viewModel.state.value.canSubmit)

        viewModel.done.test {
            viewModel.submit()
            assertEquals(PostAuthDestination.TASTES, awaitItem())
        }
        assertEquals(listOf("tok" to "Nueva4567"), auth.resetCalls)
    }

    @Test
    fun `no deja enviar si no coinciden y marca el error al salir del campo`() = runTest {
        val viewModel = newPassword()
        viewModel.onPasswordChange("Nueva4567")
        viewModel.onConfirmChange("Otra4567")
        viewModel.onConfirmBlur()

        assertFalse(viewModel.state.value.canSubmit)
        assertEquals(UiText.Res(R.string.account_password_mismatch), viewModel.state.value.confirmError)
        viewModel.submit()
        assertTrue(auth.resetCalls.isEmpty())
    }

    @Test
    fun `enlace vencido pide otro y error de la contrasena va bajo el campo`() = runTest {
        auth.resetResult = Result.Failure(AppError.Validation(emptyList(), "enlace inválido"))
        val viewModel = newPassword()
        viewModel.onPasswordChange("Nueva4567")
        viewModel.onConfirmChange("Nueva4567")
        viewModel.submit()
        advanceUntilIdle()
        assertTrue(viewModel.state.value.linkExpired)

        auth.resetResult = Result.Failure(AppError.Validation(listOf(FieldError("password_nuevo", "muy común"))))
        val second = newPassword()
        second.onPasswordChange("Nueva4567")
        second.onConfirmChange("Nueva4567")
        second.submit()
        advanceUntilIdle()
        assertFalse(second.state.value.linkExpired)
        assertEquals(UiText.Plain("Muy común"), second.state.value.passwordError)
    }

    @Test
    fun `sin red muestra el error y deja reintentar`() = runTest {
        auth.resetResult = Result.Failure(AppError.Network)
        val viewModel = newPassword()
        viewModel.onPasswordChange("Nueva4567")
        viewModel.onConfirmChange("Nueva4567")
        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `sin token el enlace se da por vencido`() {
        assertTrue(newPassword(token = "").state.value.linkExpired)
    }

    @Test
    fun `al guardar se olvida el token de un solo uso`() = runTest {
        val link = holder(EmailLink.ResetPassword("tok"))
        val viewModel = NewPasswordViewModel(link, auth)
        viewModel.onPasswordChange("Nueva4567")
        viewModel.onConfirmChange("Nueva4567")
        viewModel.submit()
        advanceUntilIdle()
        assertNull(link.resetToken())
    }

    // Verificar correo

    @Test
    fun `verificar correo pasa a verificado o a vencido`() = runTest {
        val ok = VerifyEmailViewModel(holder(EmailLink.VerifyEmail("t1")), auth)
        advanceUntilIdle()
        assertEquals(VerifyEmailPhase.VERIFIED, ok.state.value.phase)
        assertEquals(listOf("t1"), auth.verifyCalls)

        auth.verifyResult = Result.Failure(AppError.Gone())
        val expired = VerifyEmailViewModel(holder(EmailLink.VerifyEmail("t2")), auth)
        advanceUntilIdle()
        assertEquals(VerifyEmailPhase.LINK_EXPIRED, expired.state.value.phase)

        auth.verifyResult = Result.Failure(AppError.Network)
        val offline = VerifyEmailViewModel(holder(EmailLink.VerifyEmail("t3")), auth)
        advanceUntilIdle()
        assertEquals(VerifyEmailPhase.ERROR, offline.state.value.phase)
    }
}
