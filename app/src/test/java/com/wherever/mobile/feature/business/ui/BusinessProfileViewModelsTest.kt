package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.MAX_PHOTOS
import com.wherever.mobile.feature.business.domain.TaxData
import com.wherever.mobile.feature.business.domain.isValidClabe
import com.wherever.mobile.feature.business.domain.isValidPauseUntil
import com.wherever.mobile.feature.business.domain.isValidRfc
import com.wherever.mobile.feature.business.domain.isValidTaxData
import com.wherever.mobile.feature.photos.domain.ImageState
import com.wherever.mobile.feature.photos.domain.UploadedImage
import com.wherever.mobile.testing.FakeAuthRepository
import com.wherever.mobile.testing.FakeBusinessPhotoRepository
import com.wherever.mobile.testing.FakeBusinessProfileRepository
import com.wherever.mobile.testing.MainDispatcherRule
import com.wherever.mobile.testing.businessClock
import com.wherever.mobile.testing.businessToday
import com.wherever.mobile.testing.sampleBizProfile
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/** nv-38: If-Match y 409, pausa ≤ 90 días, galería de 3 a 10, CLABE 3-7-1, RFC y eliminar con el nombre exacto. */
@OptIn(ExperimentalCoroutinesApi::class)
class BusinessProfileViewModelsTest {
    @get:Rule
    val mainDispatcher = MainDispatcherRule()

    private val repository = FakeBusinessProfileRepository()
    private val auth = FakeAuthRepository()

    private fun profileViewModel() = BusinessProfileViewModel(repository, auth, businessClock)

    @Test
    fun `reglas de pausa, CLABE y RFC`() {
        assertTrue(isValidPauseUntil(null, businessToday))
        assertTrue(isValidPauseUntil(businessToday.plusDays(90), businessToday))
        assertFalse(isValidPauseUntil(businessToday.plusDays(91), businessToday))
        assertFalse(isValidPauseUntil(businessToday, businessToday))

        assertTrue(isValidClabe("012180001234567899"))
        assertFalse(isValidClabe("012180001234567895"))
        assertFalse(isValidClabe("01218000123456789"))

        assertTrue(isValidRfc("CRA180512K73"))
        assertTrue(isValidRfc("loea900101ab1"))
        assertFalse(isValidRfc("CRA181332K73"))
        assertFalse(isValidRfc("CRA18051K73"))
        assertTrue(isValidTaxData(TaxData("CRA180512K73", "Café Raíz", "601", "G03", "42800")))
        assertFalse(isValidTaxData(TaxData("CRA180512K73", "Café Raíz", "601", "G03", "4280")))
    }

    @Test
    fun `recibir reservaciones manda la version y un 409 recarga`() = runTest {
        val viewModel = profileViewModel()
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.onAcceptsReservations(false)
        advanceUntilIdle()
        assertEquals(listOf(3L), repository.versionsSent)
        assertEquals(false, viewModel.state.value.profile?.acceptsReservations)
        assertEquals(4L, viewModel.state.value.profile?.version)

        repository.acceptsResult = Result.Failure(AppError.Conflict())
        val loadsBefore = repository.calls.count { it == "perfil" }
        viewModel.onAcceptsReservations(true)
        advanceUntilIdle()
        assertEquals(loadsBefore + 1, repository.calls.count { it == "perfil" })
        assertNotNull(viewModel.state.value.message)
    }

    @Test
    fun `pausar manda fecha de hasta 90 dias y motivo, sin fecha no manda hasta`() = runTest {
        val viewModel = profileViewModel()
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.openPause()
        viewModel.onPauseLength(PauseLength.MONTH)
        viewModel.onPauseReason("Vacaciones")
        viewModel.togglePause()
        advanceUntilIdle()
        assertEquals(Triple(true, businessToday.plusDays(30), "Vacaciones"), repository.pauses.single())
        assertFalse(viewModel.state.value.pauseOpen)
        assertEquals(UiText.Res(R.string.biz_profile_paused), viewModel.state.value.message)

        viewModel.openPause()
        viewModel.onPauseLength(PauseLength.OPEN)
        viewModel.togglePause()
        advanceUntilIdle()
        assertNull(repository.pauses.last().second)
    }

    @Test
    fun `reanudar no manda fecha ni motivo`() = runTest {
        repository.profileResult = Result.Success(sampleBizProfile(paused = true, state = "pausado"))
        val viewModel = profileViewModel()
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.togglePause()
        advanceUntilIdle()
        assertEquals(Triple(false, null, null), repository.pauses.single())
    }

    @Test
    fun `eliminar solo con el nombre exacto y luego vuelve a explorar`() = runTest {
        val viewModel = profileViewModel()
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.openDelete()
        viewModel.onDeleteText("café raíz")
        assertFalse(viewModel.state.value.canDelete)
        viewModel.delete()
        advanceUntilIdle()
        assertTrue(repository.calls.none { it.startsWith("borrar ") })

        viewModel.onDeleteText("Café Raíz")
        assertTrue(viewModel.state.value.canDelete)
        viewModel.delete()
        advanceUntilIdle()
        assertTrue(repository.calls.contains("borrar Café Raíz"))
        assertTrue(repository.calls.contains("explorar"))
        assertEquals(ProfileExit.DELETED, viewModel.exits.first())
    }

    @Test
    fun `con reservas futuras no se borra y se explica`() = runTest {
        repository.deleteResult = Result.Failure(AppError.Conflict())
        val viewModel = profileViewModel()
        viewModel.start("n1")
        advanceUntilIdle()
        viewModel.openDelete()
        viewModel.onDeleteText("Café Raíz")

        viewModel.delete()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.biz_profile_delete_future), viewModel.state.value.deleteError)
        assertTrue(viewModel.state.value.deleteOpen)
    }

    @Test
    fun `volver a Wherever y cerrar sesion salen del modo negocio`() = runTest {
        val viewModel = profileViewModel()
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.explore()
        advanceUntilIdle()
        assertEquals(ProfileExit.EXPLORE, viewModel.exits.first())

        viewModel.logout()
        advanceUntilIdle()
        assertEquals(1, auth.logoutCalls)
        assertEquals(ProfileExit.LOGGED_OUT, viewModel.exits.first())
    }

    @Test
    fun `datos y horario, 7 dias, If-Match y 409 avisa`() = runTest {
        val viewModel = BusinessDataViewModel(repository)
        viewModel.start("n1")
        advanceUntilIdle()
        assertEquals(7, viewModel.state.value.schedules.size)

        viewModel.onPhone("55-1234")
        assertFalse(viewModel.state.value.canSave)
        viewModel.onPhone("5598765432")
        viewModel.onSchedule(DaySchedule(2, "09:00", "18:00", closed = false))
        viewModel.save()
        advanceUntilIdle()
        assertEquals(listOf(3L), repository.versionsSent)

        repository.updateResult = Result.Failure(AppError.Conflict())
        viewModel.save()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.biz_data_conflict), viewModel.state.value.saveError)
    }

    @Test
    fun `galeria, ordenar, portada y guardar el orden completo`() = runTest {
        val viewModel = GalleryViewModel(repository, FakeBusinessPhotoRepository(Result.Failure(AppError.Network)))
        viewModel.start("n1")
        advanceUntilIdle()
        assertFalse(viewModel.state.value.canSave)

        viewModel.move(3, 1)
        assertEquals(listOf("f1", "f4", "f2", "f3"), viewModel.state.value.photos.map { it.id })
        viewModel.makeCover(BusinessPhoto("f3", "https://img/3.jpg"))
        assertEquals("f3", viewModel.state.value.photos.first().id)
        assertTrue(viewModel.state.value.canSave)

        viewModel.save()
        advanceUntilIdle()
        assertEquals(listOf("f3", "f1", "f4", "f2"), repository.orderSent)
        assertFalse(viewModel.state.value.orderChanged)
    }

    @Test
    fun `galeria aprobada no baja de 3 fotos`() = runTest {
        repository.profileResult = Result.Success(sampleBizProfile(photos = 3))
        val viewModel = GalleryViewModel(repository, FakeBusinessPhotoRepository(Result.Failure(AppError.Network)))
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.delete(viewModel.state.value.photos.first())
        advanceUntilIdle()
        assertTrue(repository.calls.none { it.startsWith("borrar foto") })
        assertEquals(UiText.Res(R.string.gallery_min_photos), viewModel.state.value.message)
    }

    @Test
    fun `en borrador si se puede bajar de 3 y con 10 no se agrega`() = runTest {
        repository.profileResult = Result.Success(sampleBizProfile(photos = 3, state = "borrador"))
        val viewModel = GalleryViewModel(repository, FakeBusinessPhotoRepository(Result.Failure(AppError.Network)))
        viewModel.start("n1")
        advanceUntilIdle()
        viewModel.delete(viewModel.state.value.photos.first())
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.photos.size)

        repository.profileResult = Result.Success(sampleBizProfile(photos = MAX_PHOTOS))
        val full = GalleryViewModel(repository, FakeBusinessPhotoRepository(Result.Failure(AppError.Network)))
        full.start("n1")
        advanceUntilIdle()
        assertFalse(full.state.value.canAdd)
        full.openAdd()
        assertFalse(full.state.value.addOpen)
    }

    @Test
    fun `agregar foto sube y la registra, rechazada no se registra`() = runTest {
        val uploads =
            FakeBusinessPhotoRepository(Result.Success(UploadedImage("img9", ImageState.IN_REVIEW, null, null)))
        val viewModel = GalleryViewModel(repository, uploads)
        viewModel.start("n1")
        advanceUntilIdle()

        viewModel.onPicked("content://foto")
        advanceUntilIdle()
        assertEquals(listOf("content://foto"), uploads.sources)
        assertTrue(repository.calls.contains("foto img9"))
        assertEquals(5, viewModel.state.value.photos.size)

        uploads.result = Result.Success(UploadedImage("img10", ImageState.REJECTED, null, null))
        viewModel.onPicked("content://otra")
        advanceUntilIdle()
        assertFalse(repository.calls.contains("foto img10"))
        assertEquals(UiText.Res(R.string.photo_rejected), viewModel.state.value.message)
    }

    @Test
    fun `cobros, saldo, paginas con cursor y conectar abre la url`() = runTest {
        val viewModel = PaymentsViewModel(repository)
        viewModel.start("n1")
        advanceUntilIdle()
        assertEquals(342_000L, viewModel.state.value.balance?.available)
        assertEquals("1234", viewModel.state.value.account?.lastFour)
        assertEquals(1, viewModel.state.value.settlements.size)

        viewModel.loadMore()
        advanceUntilIdle()
        assertEquals(2, viewModel.state.value.settlements.size)
        assertNull(viewModel.state.value.nextCursor)

        viewModel.connect()
        advanceUntilIdle()
        assertEquals("https://connect.stripe.com/setup/abc", viewModel.connectUrl.first())

        repository.connectResult = Result.Failure(AppError.Server())
        viewModel.connect()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.payments_unavailable), viewModel.state.value.message)
    }

    @Test
    fun `banco, 18 digitos con verificador y solo queda el final`() = runTest {
        val viewModel = BankAccountViewModel(repository)
        viewModel.start("n1", "1234")
        viewModel.onHolder("Café Raíz")
        viewModel.onClabe("0121 8000 1234 5678 95")
        assertTrue(viewModel.state.value.clabeComplete)
        assertFalse(viewModel.state.value.canSave)

        viewModel.onClabe("012180001234567899")
        assertTrue(viewModel.state.value.canSave)
        viewModel.save()
        advanceUntilIdle()
        assertEquals("7899", viewModel.saved.first())
        assertEquals("", viewModel.state.value.clabe)
        assertEquals("7899", viewModel.state.value.currentLastFour)
    }

    @Test
    fun `banco con 422 clabe_invalida conserva lo escrito`() = runTest {
        repository.bankResult = Result.Failure(AppError.Validation(apiMessage = "CLABE inválida"))
        val viewModel = BankAccountViewModel(repository)
        viewModel.start("n1", null)
        viewModel.onHolder("Café Raíz")
        viewModel.onClabe("012180001234567899")

        viewModel.save()
        advanceUntilIdle()
        assertEquals(UiText.Plain("CLABE inválida"), viewModel.state.value.error)
        assertEquals("012180001234567899", viewModel.state.value.clabe)
    }

    @Test
    fun `fiscales, RFC en mayusculas y 422 rfc_invalido explicado`() = runTest {
        val viewModel = TaxDataViewModel(repository)
        viewModel.start("n1", false)
        viewModel.onRfc("cra180512k73")
        viewModel.onLegalName("Café Raíz S.A. de C.V.")
        viewModel.onPostalCode("42800")
        viewModel.onRegime("626")
        assertTrue(viewModel.state.value.canSave)

        viewModel.save()
        advanceUntilIdle()
        assertEquals("CRA180512K73", repository.taxSent?.rfc)
        assertEquals("626", repository.taxSent?.regime)
        assertEquals(true, viewModel.state.value.complete)

        repository.taxResult = Result.Failure(
            AppError.Validation(fields = listOf(com.wherever.mobile.core.common.FieldError("rfc", "rfc_invalido")))
        )
        viewModel.save()
        advanceUntilIdle()
        assertEquals(UiText.Res(R.string.tax_rfc_invalid), viewModel.state.value.error)
    }
}
