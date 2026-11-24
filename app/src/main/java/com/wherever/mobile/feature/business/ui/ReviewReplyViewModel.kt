package com.wherever.mobile.feature.business.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wherever.mobile.R
import com.wherever.mobile.core.common.AppError
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.business.domain.BusinessReviewsRepository
import com.wherever.mobile.feature.business.domain.REPLY_MAX
import com.wherever.mobile.feature.business.domain.ReplyStatus
import com.wherever.mobile.feature.business.domain.isValidReply
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ReviewReplyUiState(
    val text: String = "",
    val suggesting: Boolean = false,
    val saving: Boolean = false,
    val error: UiText? = null
) {
    val canPublish: Boolean get() = !saving && isValidReply(text)
}

/** 52_NegResp (specs/nv-36): respuesta rápida, sugerencia de la IA (editable) y Publicar. */
@HiltViewModel
class ReviewReplyViewModel @Inject constructor(private val repository: BusinessReviewsRepository) : ViewModel() {
    private val mutableState = MutableStateFlow(ReviewReplyUiState())
    val state: StateFlow<ReviewReplyUiState> = mutableState.asStateFlow()

    private val publishedChannel = Channel<Int>(Channel.BUFFERED)

    /** Texto (`R.string`) del aviso al regresar: publicada o en revisión. */
    val published = publishedChannel.receiveAsFlow()

    private var businessId: String? = null
    private var reviewId: String? = null

    fun start(business: String, review: String, current: String?) {
        if (reviewId == review) return
        businessId = business
        reviewId = review
        mutableState.update { it.copy(text = current.orEmpty().take(REPLY_MAX)) }
    }

    fun onText(text: String) = mutableState.update { it.copy(text = text.take(REPLY_MAX), error = null) }

    /** Respuesta rápida: reemplaza lo escrito con la plantilla, que la persona puede editar. */
    fun onQuick(text: String) = onText(text)

    /** La IA nunca publica sola: deja el texto en el campo para revisarlo. */
    fun suggest() {
        val business = businessId ?: return
        val review = reviewId ?: return
        mutableState.update { it.copy(suggesting = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.suggestReply(business, review)) {
                is Result.Success -> mutableState.update {
                    it.copy(suggesting = false, text = result.data.take(REPLY_MAX))
                }
                is Result.Failure -> mutableState.update {
                    it.copy(
                        suggesting = false,
                        error = if (result.error is AppError.Server) {
                            UiText.Res(R.string.reply_ai_unavailable)
                        } else {
                            result.error.toUiText()
                        }
                    )
                }
            }
        }
    }

    /** 422 de moderación: el motivo de la API, sin perder lo escrito. */
    fun publish() {
        val business = businessId ?: return
        val review = reviewId ?: return
        val current = state.value
        if (!current.canPublish) return
        mutableState.update { it.copy(saving = true, error = null) }
        viewModelScope.launch {
            when (val result = repository.reply(business, review, current.text)) {
                is Result.Success -> {
                    mutableState.update { it.copy(saving = false) }
                    publishedChannel.send(
                        if (result.data ==
                            ReplyStatus.IN_MODERATION
                        ) {
                            R.string.reply_in_moderation
                        } else {
                            R.string.reply_published
                        }
                    )
                }
                is Result.Failure -> mutableState.update { it.copy(saving = false, error = result.error.toUiText()) }
            }
        }
    }
}
