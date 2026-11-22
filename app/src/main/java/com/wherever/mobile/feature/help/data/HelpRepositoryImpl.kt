package com.wherever.mobile.feature.help.data

import android.content.Context
import androidx.annotation.StringRes
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.di.AppVersion
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.help.domain.Faq
import com.wherever.mobile.feature.help.domain.HelpRepository
import com.wherever.mobile.feature.help.domain.REPORT_MAX_LENGTH
import com.wherever.mobile.feature.help.domain.SupportContacts
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** `modulo` con el que el backend agrupa los reportes de usuarios (specs/nv-33). */
internal const val REPORT_MODULE = "reporte_usuario"

/** Preguntas frecuentes de 86_Ayuda, en el orden de la maqueta y de `specs/nv-33/plan.md` §6. */
private val FAQ_TEXTS: List<Pair<Int, Int>> = listOf(
    R.string.help_faq_cancel_q to R.string.help_faq_cancel_a,
    R.string.help_faq_deposit_q to R.string.help_faq_deposit_a,
    R.string.help_faq_seal_q to R.string.help_faq_seal_a,
    R.string.help_faq_kitty_q to R.string.help_faq_kitty_a,
    R.string.help_faq_plan_ai_q to R.string.help_faq_plan_ai_a,
    R.string.help_faq_friends_q to R.string.help_faq_friends_a,
    R.string.help_faq_location_q to R.string.help_faq_location_a,
    R.string.help_faq_coupon_q to R.string.help_faq_coupon_a,
    R.string.help_faq_delete_q to R.string.help_faq_delete_a
)

class HelpRepositoryImpl @Inject constructor(
    private val api: HelpApi,
    @ApplicationContext private val context: Context,
    @AppVersion private val version: String
) : HelpRepository {
    override fun faqs(): List<Faq> = FAQ_TEXTS.mapIndexed { index, (question, answer) ->
        Faq(id = index, question = text(question), answer = text(answer))
    }

    /**
     * `SUPPORT_EMAIL` y `SUPPORT_WHATSAPP` aún no existen en `BuildConfig`: los agrega Jorge en
     * `app/build.gradle.kts` (configuración compartida, specs/nv-33). Mientras tanto las filas no se dibujan.
     */
    override fun contacts(): SupportContacts = SupportContacts(email = null, whatsapp = null)

    override suspend fun reportProblem(message: String): Result<Unit> = safeApiCall {
        api.report(
            ErrorReportDto(
                module = REPORT_MODULE,
                message = message.trim().take(REPORT_MAX_LENGTH),
                version = version
            )
        )
    }

    private fun text(@StringRes id: Int): String = context.getString(id)
}
