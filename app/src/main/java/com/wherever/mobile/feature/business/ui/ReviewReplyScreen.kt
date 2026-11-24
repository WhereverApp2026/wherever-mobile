package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.business.domain.REPLY_MAX
import com.wherever.mobile.feature.reviews.ui.StarsRow
import java.time.ZoneId

/** Respuestas rápidas de la maqueta. */
private val QUICK_REPLIES = listOf(R.string.reply_quick_thanks, R.string.reply_quick_glad, R.string.reply_quick_more)

/** 52_NegResp (specs/nv-36). Al publicar regresa a 51 con el aviso (publicada o en revisión). */
@Composable
fun ReviewReplyRoute(
    businessId: String,
    target: ReplyTarget,
    onBack: () -> Unit,
    onPublished: (Int) -> Unit,
    viewModel: ReviewReplyViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(target.reviewId) { viewModel.start(businessId, target.reviewId, target.currentReply) }
    LaunchedEffect(viewModel) { viewModel.published.collect(onPublished) }
    ReviewReplyContent(
        state,
        target,
        ReviewReplyActions(
            onBack = onBack,
            onText = viewModel::onText,
            onQuick = viewModel::onQuick,
            onSuggest = viewModel::suggest,
            onPublish = viewModel::publish
        )
    )
}

@Composable
internal fun ReviewReplyContent(
    state: ReviewReplyUiState,
    target: ReplyTarget,
    actions: ReviewReplyActions,
    zone: ZoneId = ZoneId.systemDefault()
) {
    val error = state.error
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.reply_publish),
                onClick = actions.onPublish,
                enabled = state.canPublish,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "reply_publish"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Text(stringResource(R.string.reply_title), style = MaterialTheme.typography.headlineMedium)
        }
        SurfaceCard(modifier = Modifier.testTag("reply_review")) {
            Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
                StarsRow(target.rating)
                if (target.comment.isNotBlank()) Text(target.comment, style = MaterialTheme.typography.bodyMedium)
                Text(
                    stringResource(R.string.biz_reviews_author, target.author, formatDate(target.createdAt, zone)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Text(stringResource(R.string.reply_quick_title), style = MaterialTheme.typography.labelLarge)
        ChipGroup {
            QUICK_REPLIES.forEachIndexed { index, res ->
                val text = stringResource(res)
                SelectChip(text, state.text == text, { actions.onQuick(text) }, tag = "reply_quick_$index")
            }
        }
        WhereverOutlinedButton(
            text = stringResource(if (state.suggesting) R.string.reply_suggesting else R.string.reply_suggest),
            onClick = actions.onSuggest,
            enabled = !state.suggesting && !state.saving,
            icon = ImageVector.vectorResource(R.drawable.ic_wh_sparkle),
            modifier = Modifier.fillMaxWidth(),
            tag = "reply_suggest"
        )
        LabeledTextField(
            label = stringResource(R.string.reply_label),
            value = state.text,
            onValueChange = actions.onText,
            placeholder = stringResource(R.string.reply_placeholder),
            supporting = stringResource(R.string.reply_counter, state.text.length, REPLY_MAX),
            singleLine = false,
            tag = "reply_text"
        )
        if (error != null) ErrorBanner(error.asString())
        InfoBanner(
            text = target.progress?.let { stringResource(R.string.reply_seal_progress, it) }
                ?: stringResource(R.string.reply_seal_hint),
            tag = "reply_seal"
        )
    }
}

@Preview(name = "52_NegResp claro")
@Preview(name = "52_NegResp oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ReviewReplyPreview() {
    WhereverTheme {
        ReviewReplyContent(
            ReviewReplyUiState(text = SAMPLE_REPLY_TEXT),
            sampleReplyTarget(),
            ReviewReplyActions(),
            ZoneId.of("America/Mexico_City")
        )
    }
}
