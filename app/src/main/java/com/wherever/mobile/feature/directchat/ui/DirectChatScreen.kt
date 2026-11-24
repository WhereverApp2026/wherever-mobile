package com.wherever.mobile.feature.directchat.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MessageComposer
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.directchat.domain.Conversation
import com.wherever.mobile.feature.directchat.domain.DirectAttachment
import com.wherever.mobile.feature.directchat.domain.DirectMessage
import com.wherever.mobile.feature.directchat.domain.PlanInvite
import com.wherever.mobile.feature.groups.domain.MessageStatus
import com.wherever.mobile.feature.profile.ui.ProfileAvatar
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** ChatAmigo (specs/nv-25). */
@Composable
fun DirectChatRoute(
    onBack: () -> Unit,
    onPlan: (String) -> Unit,
    onInvitePlan: (personId: String, personName: String, conversationId: String) -> Unit,
    onGone: () -> Unit,
    viewModel: DirectChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.startPolling()
        onPauseOrDispose { viewModel.stopPolling() }
    }
    LaunchedEffect(state.gone) { if (state.gone) onGone() }
    DirectChatContent(
        state,
        DirectChatActions(
            onBack = onBack,
            onPlan = onPlan,
            onInvitePlan = {
                state.conversation?.let { onInvitePlan(state.personId, state.title.substringBefore(' '), it.id) }
            },
            onRetry = viewModel::load,
            onOlder = viewModel::loadOlder,
            onDraft = viewModel::onDraft,
            onSend = viewModel::send,
            onResend = viewModel::retry
        )
    )
}

data class DirectChatActions(
    val onBack: () -> Unit = {},
    val onPlan: (String) -> Unit = {},
    /** InvitarPlan desde el chat: el plan queda compartido aquí. */
    val onInvitePlan: () -> Unit = {},
    val onRetry: () -> Unit = {},
    val onOlder: () -> Unit = {},
    val onDraft: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onResend: (DirectMessage) -> Unit = {}
)

@Composable
fun DirectChatContent(state: DirectChatUiState, actions: DirectChatActions, zone: ZoneId = ZoneId.systemDefault()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
    ) {
        Header(state, actions.onBack)
        Column(
            modifier = Modifier.padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            WhereverOutlinedButton(
                text = stringResource(R.string.person_invite_plan),
                onClick = actions.onInvitePlan,
                enabled = state.conversation != null,
                icon = Icons.Outlined.DateRange,
                tag = "direct_invite_plan"
            )
            state.error?.let { ErrorBanner(it.asString()) }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.loading && state.messages.isEmpty() -> ListSkeleton(Modifier.padding(ScreenPadding))
                state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
                state.empty -> Text(
                    stringResource(R.string.direct_chat_empty, state.title.substringBefore(' ')),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.XXL).testTag("direct_empty")
                )
                else -> MessageList(state, actions, zone)
            }
        }
        MessageComposer(
            value = state.draft,
            onValueChange = actions.onDraft,
            onSend = actions.onSend,
            placeholder = stringResource(R.string.groups_chat_hint),
            sendDescription = stringResource(R.string.groups_chat_send),
            canSend = state.canSend
        )
    }
}

@Composable
private fun Header(state: DirectChatUiState, onBack: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = Spacing.M)
    ) {
        BackCircleButton(onClick = onBack)
        Spacer(Modifier.width(Spacing.M))
        ProfileAvatar(state.title, state.conversation?.otherPhoto, size = Sizes.AvatarS)
        Spacer(Modifier.width(Spacing.M))
        Text(
            state.title,
            style = MaterialTheme.typography.titleLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).testTag("direct_title")
        )
    }
}

@Composable
private fun MessageList(state: DirectChatUiState, actions: DirectChatActions, zone: ZoneId) {
    val listState = rememberLazyListState()
    // Lista invertida: el índice 0 es el más nuevo; al acercarse al final se piden los más viejos.
    val nearTop by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - OLDER_THRESHOLD
        }
    }
    LaunchedEffect(nearTop, state.olderCursor) { if (nearTop && state.olderCursor != null) actions.onOlder() }
    val otherName = state.title.substringBefore(' ')
    LazyColumn(
        state = listState,
        reverseLayout = true,
        // Pegados al campo de texto, como en cualquier chat, aunque sean pocos.
        verticalArrangement = Arrangement.spacedBy(Spacing.S, Alignment.Bottom),
        modifier = Modifier.fillMaxSize().padding(horizontal = ScreenPadding).testTag("direct_messages")
    ) {
        items(state.messages, key = DirectMessage::id) { message ->
            Bubble(message, state.isMine(message), otherName, zone, actions)
        }
    }
}

private const val OLDER_THRESHOLD = 3

@Composable
private fun Bubble(message: DirectMessage, mine: Boolean, otherName: String, zone: ZoneId, actions: DirectChatActions) {
    Column(
        horizontalAlignment = if (mine) Alignment.End else Alignment.Start,
        modifier = Modifier.fillMaxWidth().testTag("direct_message_${message.id}")
    ) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = if (mine) RouteBlue else MaterialTheme.colorScheme.surface,
            contentColor = if (mine) Color.White else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.widthIn(max = BubbleMaxWidth)
        ) {
            Column(
                modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.M),
                verticalArrangement = Arrangement.spacedBy(Spacing.S)
            ) {
                Text(
                    if (message.deleted) stringResource(R.string.groups_chat_deleted) else message.text,
                    style = MaterialTheme.typography.bodyLarge
                )
                message.attachment?.takeIf { it.isPlan && !message.deleted }?.let {
                    PlanCard(it, mine, otherName, actions.onPlan)
                }
            }
        }
        Meta(message, zone, actions.onResend)
    }
}

@Composable
private fun PlanCard(plan: DirectAttachment, mine: Boolean, otherName: String, onPlan: (String) -> Unit) {
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.fillMaxWidth().testTag("direct_plan_card")
    ) {
        Column(
            modifier = Modifier.padding(Spacing.M),
            verticalArrangement = Arrangement.spacedBy(Spacing.XS)
        ) {
            Text(
                plan.name ?: stringResource(R.string.direct_chat_plan),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            inviteLine(plan.invite, mine, otherName)?.let { line ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.XS)
                ) {
                    if (plan.invite == PlanInvite.ACCEPTED) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = Spacing.XS)
                        )
                    }
                    Text(
                        line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("direct_plan_invite")
                    )
                }
            }
            WhereverTextLink(
                stringResource(R.string.groups_chat_see_plan),
                { onPlan(plan.id) },
                modifier = Modifier.testTag("direct_plan_open")
            )
        }
    }
}

@Composable
private fun inviteLine(invite: PlanInvite?, mine: Boolean, otherName: String): String? = when (invite) {
    PlanInvite.SENT -> stringResource(
        if (mine) R.string.direct_chat_invite_sent else R.string.direct_chat_invite_received
    )
    PlanInvite.ACCEPTED -> if (mine) {
        stringResource(R.string.direct_chat_invite_joined, otherName)
    } else {
        stringResource(R.string.direct_chat_invite_you_joined)
    }
    null -> null
}

private val BubbleMaxWidth = Sizes.QrCode

@Composable
private fun Meta(message: DirectMessage, zone: ZoneId, onResend: (DirectMessage) -> Unit) {
    when (message.status) {
        MessageStatus.PENDING -> Text(
            stringResource(R.string.groups_chat_sending),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("direct_pending")
        )
        MessageStatus.FAILED -> Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            Text(
                stringResource(R.string.groups_chat_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
            WhereverTextLink(
                stringResource(R.string.common_retry),
                { onResend(message) },
                modifier = Modifier.testTag("direct_retry")
            )
        }
        MessageStatus.SENT -> clock(message.sentAt, zone)?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.XS)
            )
        }
    }
}

private val CLOCK = DateTimeFormatter.ofPattern("HH:mm")

private fun clock(iso: String, zone: ZoneId): String? = try {
    Instant.parse(iso).atZone(zone).format(CLOCK)
} catch (_: DateTimeParseException) {
    null
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun DirectChatPreview() {
    WhereverTheme { DirectChatContent(previewState(PlanInvite.SENT), DirectChatActions(), zone = ZoneOffset.UTC) }
}

/** ChatAmigo · la otra persona ya se unió al plan. */
@Preview
@Composable
internal fun DirectChatJoinedPreview() {
    WhereverTheme { DirectChatContent(previewState(PlanInvite.ACCEPTED), DirectChatActions(), zone = ZoneOffset.UTC) }
}

private fun previewMessage(id: String, author: String?, text: String, attachment: DirectAttachment? = null) =
    DirectMessage(id, null, author, text, attachment, false, "2026-11-24T17:5${id.last()}:00Z", MessageStatus.SENT)

private fun previewState(invite: PlanInvite) = DirectChatUiState(
    personId = "u2",
    personName = "Mariana",
    conversation = Conversation("c1", "u2", "Mariana López", null),
    loading = false,
    messages = listOfNotNull(
        previewMessage("m4", "u2", "¡Va! Ya me uní 🙌").takeIf { invite == PlanInvite.ACCEPTED },
        previewMessage(
            "m3",
            "yo",
            "¡Te invito a este plan!",
            DirectAttachment(DirectAttachment.KIND_PLAN, "p1", "Viernes de tacos", invite)
        ),
        previewMessage("m2", "u2", "¿Qué hacemos el viernes?"),
        previewMessage("m1", "yo", "Hola, Mariana")
    )
)
