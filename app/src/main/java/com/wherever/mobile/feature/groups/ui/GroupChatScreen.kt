package com.wherever.mobile.feature.groups.ui

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
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.AvatarStack
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MessageComposer
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.groups.domain.ChatMessage
import com.wherever.mobile.feature.groups.domain.GroupMember
import com.wherever.mobile.feature.groups.domain.MemberStatus
import com.wherever.mobile.feature.groups.domain.MessageStatus
import com.wherever.mobile.feature.plans.domain.Mobility
import com.wherever.mobile.feature.plans.domain.Plan
import com.wherever.mobile.feature.profile.ui.ProfileAvatar
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

/** 45_GrupoChat (specs/nv-25). */
@Composable
fun GroupChatRoute(
    onBack: () -> Unit,
    onPlan: (String) -> Unit,
    onGone: () -> Unit,
    viewModel: GroupChatViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.startPolling()
        onPauseOrDispose { viewModel.stopPolling() }
    }
    LaunchedEffect(state.gone) { if (state.gone) onGone() }
    GroupChatContent(
        state,
        GroupChatActions(
            onBack = onBack,
            onPlan = onPlan,
            onRetry = viewModel::load,
            onOlder = viewModel::loadOlder,
            onDraft = viewModel::onDraft,
            onSend = viewModel::send,
            onResend = viewModel::retry
        )
    )
}

data class GroupChatActions(
    val onBack: () -> Unit = {},
    val onPlan: (String) -> Unit = {},
    val onRetry: () -> Unit = {},
    val onOlder: () -> Unit = {},
    val onDraft: (String) -> Unit = {},
    val onSend: () -> Unit = {},
    val onResend: (ChatMessage) -> Unit = {}
)

@Composable
fun GroupChatContent(state: GroupChatUiState, actions: GroupChatActions, zone: ZoneId = ZoneId.systemDefault()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .imePadding()
    ) {
        ChatHeader(state, actions.onBack)
        Column(
            modifier = Modifier.padding(horizontal = ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            state.plan?.let { PinnedPlan(it, actions.onPlan) }
            if (state.offline) InfoBanner(stringResource(R.string.groups_chat_offline), tag = "chat_offline")
            state.error?.takeIf { !state.offline }?.let { ErrorBanner(it.asString()) }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when {
                state.loading -> ListSkeleton(Modifier.padding(ScreenPadding))
                state.loadError != null -> ErrorState(state.loadError.asString(), actions.onRetry)
                state.empty -> Text(
                    stringResource(R.string.groups_chat_empty),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.Center).padding(Spacing.XXL).testTag("chat_empty")
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
private fun ChatHeader(state: GroupChatUiState, onBack: () -> Unit) {
    val group = state.group
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(horizontal = ScreenPadding, vertical = Spacing.M)
    ) {
        BackCircleButton(onClick = onBack)
        Spacer(Modifier.width(Spacing.M))
        if (group != null) {
            AvatarStack(group.members.map(GroupMember::name), size = Sizes.AvatarS, max = 2)
            Spacer(Modifier.width(Spacing.M))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    state.title ?: groupTitle(group),
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("chat_title")
                )
                val people = group.members.count { it.status == MemberStatus.CONFIRMED }
                Text(
                    pluralStringResource(R.plurals.groups_people, people, people),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PinnedPlan(plan: Plan, onPlan: (String) -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("chat_plan")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Text(plan.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                stringResource(
                    R.string.groups_chat_plan_line,
                    formatDate("${plan.date}T12:00:00Z", ZoneOffset.UTC),
                    pluralStringResource(R.plurals.groups_stops, plan.stops.size, plan.stops.size),
                    plan.costPerPerson
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.groups_chat_see_plan),
                onClick = { onPlan(plan.id) },
                modifier = Modifier.fillMaxWidth(),
                tag = "chat_see_plan"
            )
        }
    }
}

@Composable
private fun MessageList(state: GroupChatUiState, actions: GroupChatActions, zone: ZoneId) {
    val listState = rememberLazyListState()
    // Lista invertida: el índice 0 es el más nuevo; al acercarse al final se piden los más viejos.
    val nearTop by remember {
        derivedStateOf {
            val last = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            last >= listState.layoutInfo.totalItemsCount - OLDER_THRESHOLD
        }
    }
    LaunchedEffect(nearTop, state.olderCursor) { if (nearTop && state.olderCursor != null) actions.onOlder() }
    LazyColumn(
        state = listState,
        reverseLayout = true,
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = Modifier.fillMaxSize().padding(horizontal = ScreenPadding).testTag("chat_messages")
    ) {
        items(state.messages, key = ChatMessage::id) { message ->
            val mine = message.authorId == null && !message.system || message.authorId == state.myId
            MessageBubble(message, mine, zone, actions.onResend)
        }
    }
}

private const val OLDER_THRESHOLD = 3

@Composable
private fun MessageBubble(message: ChatMessage, mine: Boolean, zone: ZoneId, onResend: (ChatMessage) -> Unit) {
    if (message.system) {
        Text(
            message.text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.XS)
        )
        return
    }
    Row(
        horizontalArrangement = if (mine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom,
        modifier = Modifier.fillMaxWidth().testTag("message_${message.id}")
    ) {
        if (!mine) {
            ProfileAvatar(message.authorName.orEmpty(), message.authorPhoto, size = Sizes.IconXXL)
            Spacer(Modifier.width(Spacing.S))
        }
        Column(horizontalAlignment = if (mine) Alignment.End else Alignment.Start) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (mine) RouteBlue else MaterialTheme.colorScheme.surface,
                contentColor = if (mine) Color.White else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.widthIn(max = BubbleMaxWidth)
            ) {
                Column(modifier = Modifier.padding(horizontal = Spacing.L, vertical = Spacing.M)) {
                    if (!mine && message.authorName != null) {
                        Text(
                            message.authorName,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Text(
                        if (message.deleted) stringResource(R.string.groups_chat_deleted) else message.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            MessageMeta(message, zone, onResend)
        }
    }
}

private val BubbleMaxWidth = Sizes.QrCode

@Composable
private fun MessageMeta(message: ChatMessage, zone: ZoneId, onResend: (ChatMessage) -> Unit) {
    when (message.status) {
        MessageStatus.PENDING -> Text(
            stringResource(R.string.groups_chat_sending),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.testTag("message_pending")
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
                modifier = Modifier.testTag("message_retry")
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
internal fun GroupChatPreview() {
    WhereverTheme {
        GroupChatContent(
            GroupChatUiState(
                group = PREVIEW_GROUPS.first(),
                plan = PREVIEW_PLAN,
                loading = false,
                myId = "yo",
                messages = PREVIEW_MESSAGES
            ),
            GroupChatActions(),
            zone = ZoneOffset.UTC
        )
    }
}

private val PREVIEW_PLAN = Plan(
    id = "pg1",
    name = "Viernes de tacos",
    date = "2026-11-27",
    people = 4,
    budget = 400,
    mobility = Mobility.WALK,
    stops = emptyList(),
    transfers = emptyList(),
    totalCost = 1280,
    costPerPerson = 320,
    isPublic = false,
    version = 1
)

private fun previewMessage(id: String, author: String?, name: String?, text: String, status: MessageStatus) =
    ChatMessage(id, null, author, name, null, false, text, false, "2026-11-20T17:5${id.last()}:00Z", status)

private val PREVIEW_MESSAGES = listOf(
    previewMessage("m4", "yo", "Yo", "No pude mandar este", MessageStatus.FAILED),
    previewMessage("m3", "yo", "Yo", "Me late, yo llevo el coche", MessageStatus.SENT),
    previewMessage("m2", "u2", "Mariana", "¿Y si vamos a las 8?", MessageStatus.SENT),
    previewMessage("m1", "u1", "Jorge", "Yo voto por los tacos 🌮", MessageStatus.SENT)
)
