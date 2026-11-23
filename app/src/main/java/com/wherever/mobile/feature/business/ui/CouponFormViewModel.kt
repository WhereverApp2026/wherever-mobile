package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BUSINESS_ZONE
import com.wherever.mobile.feature.business.domain.BusinessCouponsRepository
import com.wherever.mobile.feature.business.domain.CENTS
import com.wherever.mobile.feature.business.domain.COUPON_TITLE_MAX
import com.wherever.mobile.feature.business.domain.CouponType
import com.wherever.mobile.feature.business.domain.NewCoupon
import com.wherever.mobile.feature.business.domain.isValidCoupon
import com.wherever.mobile.feature.business.domain.isValidTime
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalTime
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Vigencia inicial de un cupón nuevo: 30 días. */
private const val DEFAULT_DAYS = 30L

/** Paso del límite de usos (−/+). */
internal const val USAGE_STEP = 10

/** Largo máximo del valor escrito (porcentaje o pesos). */
private const val VALUE_MAX_CHARS = 7

data class CouponFormUiState(
    val type: CouponType = CouponType.PERCENT,
    val value: String = "",
    val title: String = "",
    /** 0 domingo a 6 sábado; vacío = todos los días. */
    val days: Set<Int> = emptySet(),
    val hoursFrom: String = "",
    val hoursTo: String = "",
    val start: LocalDate? = null,
    val end: LocalDate? = null,
    /** 0 = sin límite. */
    val usageLimit: Int = 0,
    val suggesting: Boolean = false,
    val saving: Boolean = false,
    val error: UiText? = null
) {
    /** Porcentaje (1–100) o monto en centavos; nulo si no aplica o no es número. */
    val numericValue: Long?
        get() = when (type) {
            CouponType.PERCENT -> value.toLongOrNull()
            CouponType.AMOUNT -> value.toLongOrNull()?.times(CENTS)
            CouponType.TWO_FOR_ONE, CouponType.GIFT -> null
        }

    /** Horas opcionales, pero si se escriben van las dos en HH:MM. */
    val hoursValid: Boolean
        get() = (hoursFrom.isEmpty() && hoursTo.isEmpty()) || (isValidTime(hoursFrom) && isValidTime(hoursTo))

    val canPublish: Boolean
        get() {
            val from = start ?: return false
            val to = end ?: return false
            return !saving && hoursValid && isValidCoupon(title, type, numericValue, from, to)
        }
}

/** 54_NegCrear (specs/nv-37): tipo, valor, cuándo aplica, límite, vista previa y título sugerido por IA. */
@Suppress("TooManyFunctions") // Un evento por control del formulario.
@HiltViewModel
class CouponFormViewModel @Inject constructor(
    private val repository: BusinessCouponsRepository,
    private val clock: Clock
) : ViewModel() {
    private val today: LocalDate = LocalDate.now(clock.withZone(BUSINESS_ZONE))
    private val mutableState = MutableStateFlow(CouponFormUiState(start = today, end = today.plusDays(DEFAULT_DAYS)))
    val state: StateFlow<CouponFormUiState> = mutableState.asStateFlow()

    private val publishedChannel = Channel<Unit>(Channel.BUFFERED)
    val published = publishedChannel.receiveAsFlow()

    private var businessId: String? = null

    fun start(id: String) {
        businessId = id
    }

    fun today(): LocalDate = today

    fun onType(type: CouponType) = mutableState.update {
        it.copy(type = type, value = if (type.hasValue) it.value else "")
    }

    fun onValue(text: String) = mutableState.update {
        it.copy(value = text.filter(Char::isDigit).take(VALUE_MAX_CHARS))
    }

    fun onTitle(text: String) = mutableState.update { it.copy(title = text.take(COUPON_TITLE_MAX), error = null) }

    fun toggleDay(day: Int) = mutableState.update {
        if (day in
            it.days
        ) {
            it.copy(days = it.days - day)
        } else {
            it.copy(days = it.days + day)
        }
    }

    fun onHoursFrom(text: String) = mutableState.update { it.copy(hoursFrom = text.take(HH_MM)) }

    fun onHoursTo(text: String) = mutableState.update { it.copy(hoursTo = text.take(HH_MM)) }

    fun onEnd(date: LocalDate) = mutableState.update { it.copy(end = date) }

    fun onLimit(limit: Int) = mutableState.update { it.copy(usageLimit = limit.coerceAtLeast(0)) }

    /** «Sugerir con IA»: no publica nada; la persona puede editar lo sugerido. */
    fun suggestTitle() {
        val id = businessId ?: return
        val current = state.value
        mutableState.update { it.copy(suggesting = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.suggestTitle(id, current.type, current.numericValue, current.title)) {
                is Result.Success -> mutableState.update {
                    it.copy(suggesting = false, title = result.data.take(COUPON_TITLE_MAX))
                }
                is Result.Failure -> mutableState.update {
                    it.copy(suggesting = false, error = result.error.toUiText())
                }
            }
        }
    }

    fun publish() {
        val id = businessId ?: return
        val current = state.value
        val startDate = current.start
        val endDate = current.end
        if (!current.canPublish || startDate == null || endDate == null) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            val coupon = NewCoupon(
                title = current.title,
                type = current.type,
                value = current.numericValue,
                conditions = null,
                start = if (startDate ==
                    today
                ) {
                    OffsetDateTime.now(clock.withZone(BUSINESS_ZONE)).withNano(0)
                } else {
                    startDate.toOffset(LocalTime.MIDNIGHT)
                },
                end = endDate.toOffset(END_OF_DAY),
                days = current.days.toList(),
                hoursFrom = current.hoursFrom.takeIf(::isValidTime)?.let(LocalTime::parse),
                hoursTo = current.hoursTo.takeIf(::isValidTime)?.let(LocalTime::parse),
                usageLimit = current.usageLimit
            )
            when (val result = repository.create(id, coupon)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false) }
                    publishedChannel.send(Unit)
                }
                // Lo escrito no se pierde.
                is Result.Failure -> mutableState.update { it.copy(saving = false, error = result.error.toUiText()) }
            }
        }
    }

    private fun LocalDate.toOffset(time: LocalTime): OffsetDateTime =
        atTime(time).atZone(BUSINESS_ZONE).toOffsetDateTime()

    private companion object {
        const val HH_MM = 5
        val END_OF_DAY: LocalTime = LocalTime.of(23, 59, 59)
    }
}
