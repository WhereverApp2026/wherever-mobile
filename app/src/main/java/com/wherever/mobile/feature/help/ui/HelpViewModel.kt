package com.wherever.mobile.feature.help.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.fieldErrors
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.help.domain.Faq
import com.wherever.mobile.feature.help.domain.HelpRepository
import com.wherever.mobile.feature.help.domain.REPORT_MAX_LENGTH
import com.wherever.mobile.feature.help.domain.SupportContacts
import com.wherever.mobile.feature.help.domain.matching
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Campo del reporte en `campos[]` de un 422. */
private const val FIELD_MESSAGE = "mensaje"

/** Hoja «Reportar un problema». Lo escrito se conserva si el envío falla. */
data class ReportUiState(
    val text: String = "",
    val sending: Boolean = false,
    val error: UiText? = null,
    val fieldError: UiText? = null
) {
    val canSend: Boolean
        get() = text.isNotBlank() && text.length <= REPORT_MAX_LENGTH && !sending
}

data class HelpUiState(
    val query: String = "",
    /** Preguntas que coinciden con [query]. */
    val faqs: List<Faq> = emptyList(),
    val expandedId: Int? = null,
    val contacts: SupportContacts = SupportContacts(email = null, whatsapp = null),
    /** No nulo = la hoja de reporte está abierta. */
    val report: ReportUiState? = null
) {
    val noResults: Boolean
        get() = query.isNotBlank() && faqs.isEmpty()
}

/** 86_Ayuda (specs/nv-33): búsqueda en las preguntas frecuentes, contacto y reporte. */
@HiltViewModel
class HelpViewModel @Inject constructor(private val repository: HelpRepository) : ViewModel() {
    private val allFaqs = repository.faqs()
    private val mutableState = MutableStateFlow(HelpUiState(faqs = allFaqs, contacts = repository.contacts()))
    val state: StateFlow<HelpUiState> = mutableState.asStateFlow()

    private val sentChannel = Channel<Unit>(Channel.BUFFERED)

    /** Un aviso por cada reporte recibido por la API. */
    val reportSent: Flow<Unit> = sentChannel.receiveAsFlow()

    fun onQueryChange(value: String) = mutableState.update {
        it.copy(query = value, faqs = allFaqs.matching(value))
    }

    /** Abre la pregunta tocada y cierra la anterior; tocarla abierta la cierra. */
    fun onToggleFaq(id: Int) = mutableState.update {
        it.copy(expandedId = if (it.expandedId == id) null else id)
    }

    fun onOpenReport() = mutableState.update { it.copy(report = it.report ?: ReportUiState()) }

    fun onDismissReport() = mutableState.update { if (it.report?.sending == true) it else it.copy(report = null) }

    fun onReportChange(value: String) = mutableState.update {
        it.copy(report = it.report?.copy(text = value.take(REPORT_MAX_LENGTH), error = null, fieldError = null))
    }

    fun sendReport() {
        val report = state.value.report ?: return
        if (!report.canSend) return
        mutableState.update { it.copy(report = report.copy(sending = true, error = null, fieldError = null)) }
        viewModelScope.launch {
            when (val result = repository.reportProblem(report.text)) {
                is Result.Success -> {
                    mutableState.update { it.copy(report = null) }
                    sentChannel.send(Unit)
                }
                is Result.Failure -> onReportFailure(result.error)
            }
        }
    }

    private fun onReportFailure(error: AppError) {
        val fieldError = error.fieldErrors()[FIELD_MESSAGE]
        mutableState.update {
            it.copy(
                report = it.report?.copy(
                    sending = false,
                    fieldError = fieldError,
                    error = if (fieldError == null) error.toUiText() else null
                )
            )
        }
    }
}
