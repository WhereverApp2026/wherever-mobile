package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BUSINESS_ZONE
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.BusinessPanel
import com.wherever.mobile.feature.business.domain.BusinessStats
import com.wherever.mobile.feature.business.domain.DailyTip
import com.wherever.mobile.feature.business.domain.Reservation
import com.wherever.mobile.feature.business.domain.ReservationStatus
import com.wherever.mobile.feature.business.domain.StatsRange
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Filas de «Próximas hoy» en el Panel. */
private const val UPCOMING_ROWS = 3

/** Cifras de la portada de 48_NegPanel. Solo las que la API mandó. */
enum class PanelMetric { RESERVATIONS_TODAY, ARRIVALS_TODAY, COUPONS_TODAY }

data class PanelTile(val metric: PanelMetric, val value: Int)

data class BusinessPanelUiState(
    val loading: Boolean = true,
    val error: UiText? = null,
    val panel: BusinessPanel? = null,
    val tip: DailyTip? = null,
    val week: BusinessStats? = null,
    val upcoming: List<Reservation> = emptyList()
) {
    /** Cada dato nulo se oculta (spec §2): solo entran las cifras que llegaron. */
    val tiles: List<PanelTile>
        get() = listOfNotNull(
            panel?.reservationsToday?.let { PanelTile(PanelMetric.RESERVATIONS_TODAY, it) },
            panel?.arrivalsToday?.let { PanelTile(PanelMetric.ARRIVALS_TODAY, it) },
            panel?.couponsRedeemedToday?.let { PanelTile(PanelMetric.COUPONS_TODAY, it) }
        )
}

/** 48_NegPanel (specs/nv-35). Se pide al abrir y al volver al primer plano: la API no deja guardarlo. */
@HiltViewModel
class BusinessPanelViewModel @Inject constructor(
    private val repository: BusinessHomeRepository,
    private val clock: Clock
) : ViewModel() {
    private val _state = MutableStateFlow(BusinessPanelUiState())
    val state: StateFlow<BusinessPanelUiState> = _state.asStateFlow()

    private var businessId: String? = null

    fun start(id: String) {
        businessId = id
        load()
    }

    fun load() {
        val id = businessId ?: return
        _state.update { it.copy(loading = it.panel == null, error = null) }
        viewModelScope.launch {
            val today = LocalDate.now(clock.withZone(BUSINESS_ZONE))
            val tip = async { repository.tip(id) }
            val week = async {
                val range = StatsRange.WEEK
                repository.stats(id, today.minusDays(range.days.toLong()), today.minusDays(1), range.groupBy)
            }
            val reservations = async { repository.reservations(id, today) }
            when (val panel = repository.panel(id)) {
                is Result.Failure -> _state.update { it.copy(loading = false, error = panel.error.toUiText()) }
                is Result.Success -> _state.update {
                    BusinessPanelUiState(
                        loading = false,
                        panel = panel.data,
                        // El consejo, la gráfica y las próximas son extras: si fallan, se ocultan.
                        tip = (tip.await() as? Result.Success)?.data?.takeIf { t -> t.text.isNotBlank() },
                        week = (week.await() as? Result.Success)?.data,
                        upcoming = (reservations.await() as? Result.Success)?.data?.items.orEmpty().upcoming()
                    )
                }
            }
        }
    }

    private fun List<Reservation>.upcoming(): List<Reservation> {
        val now = LocalDateTime.now(clock.withZone(BUSINESS_ZONE))
        return filter {
            it.status in setOf(ReservationStatus.PENDING, ReservationStatus.CONFIRMED) && !it.dateTime.isBefore(now)
        }.sortedBy { it.dateTime }.take(UPCOMING_ROWS)
    }
}
