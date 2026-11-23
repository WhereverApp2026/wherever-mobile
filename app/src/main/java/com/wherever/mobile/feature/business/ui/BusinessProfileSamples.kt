@file:Suppress("MagicNumber")

package com.wherever.mobile.feature.business.ui

import com.wherever.mobile.feature.business.domain.Balance
import com.wherever.mobile.feature.business.domain.BusinessPhoto
import com.wherever.mobile.feature.business.domain.BusinessProfile
import com.wherever.mobile.feature.business.domain.DaySchedule
import com.wherever.mobile.feature.business.domain.DepositAccount
import com.wherever.mobile.feature.business.domain.Settlement
import com.wherever.mobile.feature.business.domain.TaxData
import java.time.LocalDate

/** Datos de muestra de 58, 87, 57, 89 y 90: solo vistas previas, capturas y pruebas. */
internal fun sampleBusinessProfile() = BusinessProfile(
    id = "n1",
    name = "Café Raíz",
    category = "cafeteria",
    description = "Café de especialidad y pan de masa madre en el centro.",
    phone = "5512345678",
    address = "Av. Hidalgo 23, Centro, Tula",
    state = "aprobado",
    paused = false,
    schedules = (0 until 7).map { DaySchedule(it, "08:00", "20:00", closed = it == 0) },
    photos = (1..5).map { BusinessPhoto("f$it", "") },
    acceptsReservations = true,
    paymentsState = "activo",
    version = 4,
    depositAccount = DepositAccount("1234", "BBVA México", "Café Raíz S.A. de C.V.", "registrada"),
    taxDataComplete = true
)

internal fun sampleBusinessData() = BusinessDataUiState(
    loading = false,
    profile = sampleBusinessProfile(),
    description = sampleBusinessProfile().description,
    phone = sampleBusinessProfile().phone,
    schedules = sampleBusinessProfile().schedules
)

internal fun sampleGallery() = GalleryUiState(
    loading = false,
    photos = sampleBusinessProfile().photos,
    savedOrder = sampleBusinessProfile().photos.map { it.id },
    keepsMinPhotos = true
)

internal fun samplePayments() = PaymentsUiState(
    loading = false,
    balance = Balance(available = 342_000, pending = 124_000, nextDeposit = LocalDate.of(2026, 11, 30)),
    account = sampleBusinessProfile().depositAccount,
    connected = true,
    settlements = listOf(
        Settlement("s1", LocalDate.of(2026, 10, 12), 218_000, "pagada"),
        Settlement("s2", LocalDate.of(2026, 10, 5), 194_000, "pagada"),
        Settlement("s3", LocalDate.of(2026, 9, 28), 231_000, "pagada")
    ),
    nextCursor = "c2"
)

internal fun sampleBankAccount() = BankAccountUiState(
    holder = "Café Raíz S.A. de C.V.",
    clabe = "012180001234567899",
    currentLastFour = "1234"
)

internal fun sampleTaxData() = TaxDataUiState(
    data = TaxData(
        rfc = "CRA180512K73",
        legalName = "Café Raíz S.A. de C.V.",
        regime = "601",
        cfdiUse = "G03",
        postalCode = "42800"
    ),
    complete = true
)
