package com.wherever.mobile.feature.account.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.FieldError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.account.domain.AccountRepository
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.sampleProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

private class FakeAccountRepository : AccountRepository {
    var passwordResult: Result<Unit> = Result.Success(Unit)
    var emailResult: Result<Unit> = Result.Success(Unit)
    var exportResult: Result<Unit> = Result.Success(Unit)
    var clearResult: Result<Unit> = Result.Success(Unit)
    val calls = mutableListOf<String>()

    override suspend fun changePassword(current: String, new: String): Result<Unit> {
        calls += "password:$current>$new"
        return passwordResult
    }

    override suspend fun changeEmail(newEmail: String, password: String): Result<Unit> {
        calls += "email:$newEmail"
        return emailResult
    }

    override suspend fun requestDataExport(): Result<Unit> {
        calls += "export"
        return exportResult
    }

    override suspend fun clearHistory(): Result<Unit> {
        calls += "clear"
        return clearResult
    }
}

/** nv-51: cambiar contraseña y correo, descargar mis datos y borrar historial. */
@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeAccountRepository()

    private fun ChangePasswordViewModel.fill(current: String = "Actual123", new: String = "Nueva4567", confirm: String = new) {
        onCurrentChange(current)
        onNewChange(new)
        onConfirmChange(confirm)
    }

    @Test
    fun `contrasena valida se cambia`() = runTest(mainDispatcher.dispatcher) {
        val vm = ChangePasswordViewModel(repository)
        vm.fill()
        assertTrue(vm.state.value.canSubmit)

        vm.submit()
        advanceUntilIdle()

        assertEquals(listOf("password:Actual123>Nueva4567"), repository.calls)
        assertTrue(vm.state.value.done)
    }

    @Test
    fun `contrasena debil o distinta no se puede enviar`() = runTest(mainDispatcher.dispatcher) {
        val vm = ChangePasswordViewModel(repository)
        vm.fill(new = "corta")
        vm.onNewBlur()
        assertFalse(vm.state.value.canSubmit)
        assertEquals(UiText.Res(R.string.validation_password), vm.state.value.newError)

        vm.fill(confirm = "Otra45678")
        vm.onConfirmBlur()
        assertFalse(vm.state.value.canSubmit)
        assertEquals(UiText.Res(R.string.account_password_mismatch), vm.state.value.confirmError)
    }

    @Test
    fun `nueva igual a la actual no llama a la API`() = runTest(mainDispatcher.dispatcher) {
        val vm = ChangePasswordViewModel(repository)
        vm.fill(current = "Igual1234", new = "Igual1234")

        vm.submit()
        advanceUntilIdle()

        assertTrue(repository.calls.isEmpty())
        assertEquals(UiText.Res(R.string.account_password_same), vm.state.value.newError)
    }

    @Test
    fun `401 marca la contrasena actual`() = runTest(mainDispatcher.dispatcher) {
        repository.passwordResult = Result.Failure(AppError.Unauthorized("credenciales inválidas"))
        val vm = ChangePasswordViewModel(repository)
        vm.fill()

        vm.submit()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.account_wrong_password), vm.state.value.currentError)
        assertFalse(vm.state.value.done)
        assertFalse(vm.state.value.submitting)
    }

    @Test
    fun `422 va bajo su campo`() = runTest(mainDispatcher.dispatcher) {
        repository.passwordResult = Result.Failure(
            AppError.Validation(fields = listOf(FieldError("password_nuevo", "contraseña muy común")))
        )
        val vm = ChangePasswordViewModel(repository)
        vm.fill()

        vm.submit()
        advanceUntilIdle()

        assertEquals(UiText.Plain("Contraseña muy común"), vm.state.value.newError)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `correo nuevo se cambia y muestra a donde llego el enlace`() = runTest(mainDispatcher.dispatcher) {
        val vm = ChangeEmailViewModel(repository, FakeProfileRepository(sampleProfile()))
        advanceUntilIdle()
        vm.onEmailChange("nuevo @ejemplo.com")
        vm.onPasswordChange("Actual123")

        vm.submit()
        advanceUntilIdle()

        assertEquals(listOf("email:nuevo@ejemplo.com"), repository.calls)
        assertEquals("nuevo@ejemplo.com", vm.state.value.sentTo)
    }

    @Test
    fun `el mismo correo no se puede enviar`() = runTest(mainDispatcher.dispatcher) {
        val vm = ChangeEmailViewModel(repository, FakeProfileRepository(sampleProfile()))
        advanceUntilIdle()
        vm.onEmailChange("ANA@ejemplo.com")
        vm.onPasswordChange("Actual123")
        vm.onEmailBlur()

        assertFalse(vm.state.value.canSubmit)
        assertEquals(UiText.Res(R.string.account_email_same), vm.state.value.emailError)
    }

    @Test
    fun `409 y 401 del correo van a su campo`() = runTest(mainDispatcher.dispatcher) {
        val vm = ChangeEmailViewModel(repository, FakeProfileRepository(sampleProfile()))
        advanceUntilIdle()
        vm.onEmailChange("otro@ejemplo.com")
        vm.onPasswordChange("Actual123")

        repository.emailResult = Result.Failure(AppError.Conflict("correo ya registrado"))
        vm.submit()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.account_email_taken), vm.state.value.emailError)

        repository.emailResult = Result.Failure(AppError.Unauthorized())
        vm.submit()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.account_wrong_password), vm.state.value.passwordError)
        assertNull(vm.state.value.sentTo)
    }

    @Test
    fun `exportar avisa que llega por correo y el 429 no es error`() = runTest(mainDispatcher.dispatcher) {
        val vm = MyDataViewModel(repository)

        vm.requestExport()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.my_data_export_sent), vm.state.value.message)

        repository.exportResult = Result.Failure(AppError.RateLimited(retryAfterSeconds = 3600))
        vm.requestExport()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.my_data_export_once_a_day), vm.state.value.message)
        assertNull(vm.state.value.error)
    }

    @Test
    fun `borrar historial pide confirmacion`() = runTest(mainDispatcher.dispatcher) {
        val vm = MyDataViewModel(repository)

        vm.askClear()
        assertTrue(vm.state.value.confirmClear)
        vm.dismissClear()
        advanceUntilIdle()
        assertTrue(repository.calls.isEmpty())

        vm.askClear()
        vm.confirmClear()
        advanceUntilIdle()
        assertEquals(listOf("clear"), repository.calls)
        assertEquals(UiText.Res(R.string.my_data_history_cleared), vm.state.value.message)
    }

    @Test
    fun `falla al borrar muestra el error`() = runTest(mainDispatcher.dispatcher) {
        repository.clearResult = Result.Failure(AppError.Server())
        val vm = MyDataViewModel(repository)

        vm.askClear()
        vm.confirmClear()
        advanceUntilIdle()

        assertEquals(UiText.Res(R.string.error_server), vm.state.value.error)
        assertFalse(vm.state.value.clearing)
    }
}
