package com.wherever.mobile.feature.friends.ui

import androidx.lifecycle.SavedStateHandle
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.domain.PersonDetail
import com.wherever.mobile.feature.friends.domain.QrImageRepository
import com.wherever.mobile.feature.friends.domain.Relation
import com.wherever.mobile.testing.FakeFriendsRepository
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
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** nv-20 (Unit): 25_MiCodigo con carga, error, vacío, sin red, compartir, guardar y escanear. */
@OptIn(ExperimentalCoroutinesApi::class)
class MyCodeViewModelTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private lateinit var profiles: FakeProfileRepository
    private lateinit var friends: FakeFriendsRepository
    private lateinit var images: FakeQrImages

    private val withCode = sampleProfile().copy(username = "ana_l", friendCode = "K7M2P9QX")

    @Before
    fun setUp() {
        profiles = FakeProfileRepository()
        friends = FakeFriendsRepository()
        images = FakeQrImages()
    }

    private fun viewModel(scan: Boolean = false) =
        MyCodeViewModel(SavedStateHandle(mapOf("scan" to scan)), profiles, friends, images)

    @Test
    fun `muestra el codigo del perfil con el QR solo del codigo`() = runTest {
        profiles.refreshResult = Result.Success(withCode)

        val vm = viewModel()
        advanceUntilIdle()

        val state = vm.state.value
        assertFalse(state.loading)
        assertEquals("K7M2P9QX", state.code)
        assertEquals("ana_l", state.username)
        assertEquals("wherever:amigo:K7M2P9QX", state.payload)
        assertEquals(MyCodeTab.MY_CODE, state.tab)
    }

    @Test
    fun `sin red y sin perfil guardado muestra error y Reintentar lo vuelve a pedir`() = runTest {
        profiles.refreshResult = Result.Failure(AppError.Network)

        val vm = viewModel()
        advanceUntilIdle()
        assertTrue(vm.state.value.loadError != null)
        assertFalse(vm.state.value.empty)

        profiles.refreshResult = Result.Success(withCode)
        vm.refresh()
        advanceUntilIdle()

        assertNull(vm.state.value.loadError)
        assertEquals("K7M2P9QX", vm.state.value.code)
    }

    @Test
    fun `sin red con el codigo en Room lo sigue mostrando`() = runTest {
        profiles.profile.value = withCode
        profiles.refreshResult = Result.Failure(AppError.Network)

        val vm = viewModel()
        advanceUntilIdle()

        assertNull(vm.state.value.loadError)
        assertEquals("K7M2P9QX", vm.state.value.code)
    }

    @Test
    fun `perfil sin codigo queda vacio`() = runTest {
        profiles.refreshResult = Result.Success(sampleProfile().copy(friendCode = null))

        val vm = viewModel()
        advanceUntilIdle()

        assertTrue(vm.state.value.empty)
    }

    @Test
    fun `compartir entrega el content uri una vez y guardar avisa`() = runTest {
        profiles.refreshResult = Result.Success(withCode)
        val vm = viewModel()
        advanceUntilIdle()

        vm.share()
        advanceUntilIdle()
        assertEquals("content://qr", vm.state.value.shareUri)
        assertEquals(listOf("wherever:amigo:K7M2P9QX"), images.payloads)
        vm.shareHandled()
        assertNull(vm.state.value.shareUri)

        vm.save()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.my_code_saved), vm.state.value.message)

        images.saveResult = Result.Failure(AppError.Forbidden())
        vm.save()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.my_code_save_old), vm.state.value.message)
    }

    @Test
    fun `escanear un codigo valido abre a la persona`() = runTest {
        profiles.refreshResult = Result.Success(withCode)
        friends.codeResult = Result.Success(PersonDetail(Person("u2", "Luis", "luis", null), Relation.NONE))
        val vm = viewModel(scan = true)
        advanceUntilIdle()
        assertEquals(MyCodeTab.SCAN, vm.state.value.tab)

        vm.onScanned("wherever:amigo:abcd2345")
        advanceUntilIdle()

        assertEquals("u2", vm.state.value.foundPersonId)
        vm.personOpened()
        assertNull(vm.state.value.foundPersonId)
    }

    @Test
    fun `escaneo cancelado, ajeno, propio o inexistente no navega`() = runTest {
        profiles.refreshResult = Result.Success(withCode)
        val vm = viewModel(scan = true)
        advanceUntilIdle()

        vm.onScanned(null)
        assertNull(vm.state.value.scanError)

        vm.onCameraDenied()
        assertEquals(UiText.Res(R.string.my_code_camera_denied), vm.state.value.scanError)

        vm.onScanned("https://malicioso.example/login")
        assertEquals(UiText.Res(R.string.my_code_scan_invalid), vm.state.value.scanError)

        vm.onScanned("wherever:amigo:K7M2P9QX")
        assertEquals(UiText.Res(R.string.my_code_scan_own), vm.state.value.scanError)

        vm.onScanned("ABCD2345")
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.friends_code_not_found), vm.state.value.scanError)
        assertNull(vm.state.value.foundPersonId)
    }

    private class FakeQrImages : QrImageRepository {
        val payloads = mutableListOf<String>()
        var saveResult: Result<Unit> = Result.Success(Unit)

        override suspend fun shareable(payload: String): Result<String> {
            payloads += payload
            return Result.Success("content://qr")
        }

        override suspend fun saveToGallery(payload: String): Result<Unit> = saveResult
    }
}
