package com.wherever.mobile.feature.auth.ui

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.TEST_PASSWORD
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelsTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val repository = FakeAuthRepository()
    private val clock = Clock.fixed(Instant.parse("2026-11-04T12:00:00Z"), ZoneOffset.UTC)

    // Login

    private fun filledLogin() = LoginViewModel(repository).apply {
        onEmailChange("ana@ejemplo.com")
        onPasswordChange(TEST_PASSWORD)
    }

    @Test
    fun `login exitoso emite el destino y borra la contrasena`() = runTest {
        val viewModel = filledLogin()
        repository.loginResult = Result.Success(PostAuthDestination.TASTES)

        viewModel.destinations.test {
            viewModel.submit()
            assertTrue(viewModel.state.value.submitting)
            assertEquals(PostAuthDestination.TASTES, awaitItem())
        }
        assertEquals("", viewModel.state.value.password)
        assertFalse(viewModel.state.value.submitting)
    }

    @Test
    fun `login 401 muestra el mensaje de la API`() = runTest {
        val viewModel = filledLogin()
        repository.loginResult = Result.Failure(AppError.Unauthorized("correo o contraseña incorrectos"))

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Plain("Correo o contraseña incorrectos"), viewModel.state.value.error)
    }

    @Test
    fun `login 423 avisa del bloqueo de 15 minutos`() = runTest {
        val viewModel = filledLogin()
        repository.loginResult = Result.Failure(AppError.Locked("bloqueado"))

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.login_error_locked), viewModel.state.value.error)
    }

    @Test
    fun `login 429 respeta Retry-After y vuelve a habilitar el boton`() = runTest {
        val viewModel = filledLogin()
        repository.loginResult = Result.Failure(AppError.RateLimited(30))

        viewModel.submit()
        runCurrent()
        assertEquals(30L, viewModel.state.value.retryAfterSeconds)
        assertFalse(viewModel.state.value.canSubmit)

        advanceTimeBy(10_000)
        runCurrent()
        assertEquals(20L, viewModel.state.value.retryAfterSeconds)

        advanceUntilIdle()
        assertEquals(0L, viewModel.state.value.retryAfterSeconds)
        assertTrue(viewModel.state.value.canSubmit)
    }

    @Test
    fun `login 422 muestra el error bajo su campo`() = runTest {
        val viewModel = filledLogin()
        repository.loginResult = Result.Failure(
            AppError.Validation(listOf(FieldError("correo", "no es un correo válido")))
        )

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Plain("No es un correo válido"), viewModel.state.value.emailError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `login sin red usa texto propio`() = runTest {
        val viewModel = filledLogin()
        repository.loginResult = Result.Failure(AppError.Network)

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.error_network), viewModel.state.value.error)
    }

    @Test
    fun `login no envia con correo invalido`() = runTest {
        val viewModel = LoginViewModel(repository).apply {
            onEmailChange("malo")
            onPasswordChange(TEST_PASSWORD)
            onEmailBlur()
        }
        assertFalse(viewModel.state.value.canSubmit)
        assertEquals(UiText.Res(R.string.validation_email), viewModel.state.value.emailError)
    }

    // Registro

    private fun filledRegister() = RegisterViewModel(repository, clock).apply {
        onNameChange("Ana López")
        onBirthDateChange(LocalDate.of(1999, 4, 12))
        onEmailChange("ana@ejemplo.com")
        onPasswordChange(TEST_PASSWORD)
        onTermsChange(true)
    }

    @Test
    fun `registro valida en vivo nombre, edad y contrasena`() = runTest {
        val viewModel = RegisterViewModel(repository, clock)

        viewModel.onNameChange("A")
        assertEquals(UiText.Res(R.string.validation_name), viewModel.state.value.nameError)
        viewModel.onNameChange("Ana")
        assertNull(viewModel.state.value.nameError)

        viewModel.onBirthDateChange(LocalDate.of(2010, 1, 1))
        assertEquals(UiText.Res(R.string.validation_adult), viewModel.state.value.birthDateError)

        viewModel.onPasswordChange("corta")
        assertEquals(UiText.Res(R.string.validation_password), viewModel.state.value.passwordError)
    }

    @Test
    fun `registro exige aceptar terminos`() = runTest {
        val viewModel = filledRegister()
        assertTrue(viewModel.state.value.canSubmit)
        viewModel.onTermsChange(false)
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `correo se comprueba al salir del campo`() = runTest {
        val viewModel = filledRegister()
        repository.emailAvailable = Result.Success(false)

        viewModel.onEmailBlur()
        advanceUntilIdle()

        assertEquals(listOf("ana@ejemplo.com"), repository.emailChecks)
        assertEquals(EmailCheck.TAKEN, viewModel.state.value.emailCheck)
        assertEquals(UiText.Res(R.string.register_error_email_taken), viewModel.state.value.emailError)
        assertFalse(viewModel.state.value.canSubmit)
    }

    @Test
    fun `correo invalido no llama a la API`() = runTest {
        val viewModel = RegisterViewModel(repository, clock)
        viewModel.onEmailChange("malo")

        viewModel.onEmailBlur()
        advanceUntilIdle()

        assertTrue(repository.emailChecks.isEmpty())
        assertEquals(UiText.Res(R.string.validation_email), viewModel.state.value.emailError)
    }

    @Test
    fun `registro exitoso manda la fecha en formato ISO`() = runTest {
        val viewModel = filledRegister()

        viewModel.registered.test {
            viewModel.submit()
            awaitItem()
        }

        assertEquals("1999-04-12", repository.registrations.single().birthDate)
    }

    @Test
    fun `registro 409 marca el correo como ocupado`() = runTest {
        val viewModel = filledRegister()
        repository.registerResult = Result.Failure(AppError.Conflict("ya existe"))

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.register_error_email_taken), viewModel.state.value.emailError)
        assertNull(viewModel.state.value.error)
    }

    @Test
    fun `registro 422 reparte los errores por campo`() = runTest {
        val viewModel = filledRegister()
        repository.registerResult = Result.Failure(
            AppError.Validation(listOf(FieldError("fecha_nacimiento", "debes ser mayor")))
        )

        viewModel.submit()
        advanceUntilIdle()

        assertEquals(UiText.Plain("Debes ser mayor"), viewModel.state.value.birthDateError)
    }

    // Recuperar

    @Test
    fun `recuperar emite el correo tras el 202`() = runTest {
        val viewModel = RecoverViewModel(repository).apply { onEmailChange(" ana@ejemplo.com ") }

        viewModel.sent.test {
            viewModel.submit()
            assertEquals("ana@ejemplo.com", awaitItem())
        }
    }

    @Test
    fun `recuperar 429 respeta Retry-After`() = runTest {
        val viewModel = RecoverViewModel(repository).apply { onEmailChange("ana@ejemplo.com") }
        repository.recoverResult = Result.Failure(AppError.RateLimited(12))

        viewModel.submit()
        runCurrent()

        assertEquals(12L, viewModel.state.value.retryAfterSeconds)
    }

    // Revisa tu correo

    private fun checkEmail(mode: CheckEmailMode) = CheckEmailViewModel(
        SavedStateHandle(mapOf("email" to "osmar@correo.com", "mode" to mode.name)),
        repository
    )

    @Test
    fun `recuperacion arranca con 60 segundos y reenvia a recuperar`() = runTest {
        val viewModel = checkEmail(CheckEmailMode.RECOVERY)
        runCurrent()
        assertEquals("os•••@correo.com", viewModel.state.value.maskedEmail)
        assertEquals(60L, viewModel.state.value.secondsLeft)

        viewModel.resend()
        runCurrent()
        assertTrue(repository.recoverCalls.isEmpty())

        advanceTimeBy(60_000)
        runCurrent()
        assertTrue(viewModel.state.value.canResend)

        viewModel.resend()
        runCurrent()
        assertEquals(listOf("osmar@correo.com"), repository.recoverCalls)
        assertEquals(0, repository.resendCalls)
        assertEquals(60L, viewModel.state.value.secondsLeft)
    }

    @Test
    fun `verificacion puede reenviar de inmediato y usa su endpoint`() = runTest {
        val viewModel = checkEmail(CheckEmailMode.VERIFICATION)
        runCurrent()
        assertTrue(viewModel.state.value.canResend)

        viewModel.resend()
        runCurrent()

        assertEquals(1, repository.resendCalls)
        assertTrue(repository.recoverCalls.isEmpty())
        assertTrue(viewModel.state.value.resent)
    }

    @Test
    fun `reenviar 429 usa el tiempo de Retry-After`() = runTest {
        val viewModel = checkEmail(CheckEmailMode.VERIFICATION)
        repository.resendResult = Result.Failure(AppError.RateLimited(25))

        viewModel.resend()
        runCurrent()

        assertEquals(25L, viewModel.state.value.secondsLeft)
    }
}
