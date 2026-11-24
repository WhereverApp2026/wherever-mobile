package com.wherever.mobile.feature.business.ui

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverIcons
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.Settlement

/** 57_NegCobros (specs/nv-38). Se vuelve a pedir al regresar de 89_NegBanco. */
@Composable
internal fun PaymentsRoute(
    businessId: String,
    onBack: () -> Unit,
    onBank: (String?) -> Unit,
    viewModel: PaymentsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    LifecycleResumeEffect(businessId) {
        viewModel.start(businessId)
        onPauseOrDispose { }
    }
    // Sin `androidx.browser` en el stack: el registro de Stripe se abre en el navegador (D-38c).
    LaunchedEffect(viewModel) {
        viewModel.connectUrl.collect { url ->
            try {
                context.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
            } catch (_: ActivityNotFoundException) {
                // Sin navegador no hay a dónde mandar el registro; el botón queda para reintentar.
            }
        }
    }
    PaymentsContent(
        state,
        PaymentsActions(
            onBack = onBack,
            onRetry = viewModel::load,
            onMore = viewModel::loadMore,
            onConnect = viewModel::connect,
            onBank = { onBank(state.account?.lastFour) },
            onMessageShown = viewModel::messageShown
        )
    )
}

@Composable
internal fun PaymentsContent(state: PaymentsUiState, actions: PaymentsActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val error = state.error
    val balance = state.balance
    FormScreen(
        footer = {
            WhereverSnackbarHost(snackbar)
            WhereverOutlinedButton(
                text = stringResource(if (state.connected) R.string.payments_manage else R.string.payments_connect),
                onClick = actions.onConnect,
                enabled = !state.connecting,
                modifier = Modifier.fillMaxWidth(),
                tag = "payments_connect"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Text(stringResource(R.string.payments_title), style = MaterialTheme.typography.headlineMedium)
        }
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            balance != null -> {
                Surface(
                    shape = WhereverShapes.Card,
                    color = RouteBlue,
                    contentColor = Color.White,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(Spacing.L),
                        verticalArrangement = Arrangement.spacedBy(Spacing.XS)
                    ) {
                        Text(stringResource(R.string.payments_available), style = MaterialTheme.typography.bodySmall)
                        Text(
                            balance.available.asMoney(),
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.testTag("payments_available")
                        )
                        Text(
                            state.account?.let { stringResource(R.string.payments_deposit_to, it.lastFour) }
                                ?: stringResource(R.string.payments_no_account),
                            style = MaterialTheme.typography.bodySmall
                        )
                        balance.nextDeposit?.let {
                            Text(
                                stringResource(R.string.payments_next, it.shortText()),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                Surface(
                    shape = WhereverShapes.Card,
                    color = GoldTint,
                    contentColor = Ink,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.L)) {
                        Icon(Icons.Outlined.Lock, contentDescription = null, modifier = Modifier.size(Sizes.IconM))
                        Spacer(Modifier.width(Spacing.M))
                        Column {
                            Text(
                                stringResource(R.string.payments_pending, balance.pending.asMoney()),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                stringResource(R.string.payments_pending_hint),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                WhereverTextLink(
                    stringResource(
                        if (state.account ==
                            null
                        ) {
                            R.string.payments_add_bank
                        } else {
                            R.string.payments_change_bank
                        }
                    ),
                    onClick = actions.onBank,
                    modifier = Modifier.testTag("payments_bank")
                )
                Deposits(state, actions)
            }
        }
    }
}

@Composable
private fun Deposits(state: PaymentsUiState, actions: PaymentsActions) {
    SurfaceCard(modifier = Modifier.testTag("payments_deposits")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            Text(
                stringResource(R.string.payments_deposits),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            if (state.settlements.isEmpty()) {
                Text(
                    stringResource(R.string.payments_deposits_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.testTag("payments_empty")
                )
            }
            state.settlements.forEachIndexed { index, settlement ->
                if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                DepositRow(settlement)
            }
            if (state.nextCursor != null) {
                WhereverTextLink(
                    stringResource(R.string.payments_more),
                    onClick = actions.onMore,
                    enabled = !state.loadingMore,
                    modifier = Modifier.testTag("payments_more")
                )
            }
        }
    }
}

@Composable
private fun DepositRow(settlement: Settlement) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().testTag("settlement_${settlement.id}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(Sizes.IconXL).clip(CircleShape).background(MintTint)
        ) {
            Icon(WhereverIcons.Download, contentDescription = null, tint = Ink, modifier = Modifier.size(Sizes.IconS))
        }
        Spacer(Modifier.width(Spacing.M))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                stringResource(R.string.payments_weekly),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                stringResource(
                    R.string.payments_week_of,
                    settlement.weekStart.shortText(),
                    stringResource(settlement.stateLabel())
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(settlement.net.asMoney(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
    }
}

private fun Settlement.stateLabel(): Int = when (state) {
    "pagada", "transferida", "depositada" -> R.string.payments_state_paid
    "pendiente" -> R.string.payments_state_pending
    else -> R.string.payments_state_calculated
}

@Preview(name = "57_NegCobros claro")
@Preview(name = "57_NegCobros oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun PaymentsPreview() {
    WhereverTheme { PaymentsContent(samplePayments(), PaymentsActions()) }
}
