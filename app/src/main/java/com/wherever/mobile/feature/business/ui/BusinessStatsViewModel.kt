package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BUSINESS_ZONE
import com.wherever.mobile.feature.business.domain.BusinessHomeRepository
import com.wherever.mobile.feature.business.domain.BusinessStats
import com.wherever.mobile.feature.business.domain.StatsRange
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BusinessStatsUiState(
    val range: StatsRange = StatsRange.WEEK,
    val loading: Boolean = true,
    val error: UiText? = null,
    val stats: BusinessStats? = null,
    val downloading: Boolean = false,
    val message: UiText? = null
)

/** 56_NegStats: Semana, Mes y Trimestre (90 días, el máximo de la API) y el reporte mensual en PDF. */
@HiltViewModel
class BusinessStatsViewModel @Inject constructor(
    private val repository: BusinessHomeRepository,
    private val clock: Clock
) : ViewModel() {
    private val _state = MutableStateFlow(BusinessStatsUiState())
    val state: StateFlow<BusinessStatsUiState> = _state.asStateFlow()

    private var businessId: String? = null
    private var loadJob: Job? = null

    fun start(id: String) {
        if (businessId == id) return
        businessId = id
        load()
    }

    fun selectRange(range: StatsRange) {
        if (range == _state.value.range) return
        _state.update { it.copy(range = range) }
        load()
    }

    fun load() {
        val id = businessId ?: return
        val range = _state.value.range
        _state.update { it.copy(loading = true, error = null) }
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            // La API cuenta hasta ayer (días completos) en la hora de la Ciudad de México.
            val to = LocalDate.now(clock.withZone(BUSINESS_ZONE)).minusDays(1)
            val from = to.minusDays(range.days - 1L)
            when (val result = repository.stats(id, from, to, range.groupBy)) {
                is Result.Success -> _state.update { it.copy(loading = false, stats = result.data) }
                is Result.Failure -> _state.update { it.copy(loading = false, error = result.error.toUiText()) }
            }
        }
    }

    fun downloadReport() {
        val id = businessId ?: return
        if (_state.value.downloading) return
        _state.update { it.copy(downloading = true) }
        viewModelScope.launch {
            val month = YearMonth.now(clock.withZone(BUSINESS_ZONE))
            val message = when (val result = repository.downloadMonthlyReport(id, month)) {
                is Result.Success -> UiText.Res(R.string.business_stats_report_saved, listOf(result.data))
                is Result.Failure -> result.error.toUiText()
            }
            _state.update { it.copy(downloading = false, message = message) }
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }
}
