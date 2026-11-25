package com.wherever.mobile.feature.coupons.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.coupons.domain.CouponsTab
import com.wherever.mobile.feature.coupons.domain.WalletCoupon

/** Lo que la pantalla puede pedir; la navegación la resuelve el NavHost. */
data class CouponsActions(
    val onBack: () -> Unit = {},
    val onTab: (CouponsTab) -> Unit = {},
    val onOpen: (WalletCoupon) -> Unit = {},
    val onExplore: () -> Unit = {},
    val onLoadMore: () -> Unit = {},
    val onRetry: () -> Unit = {}
)

/** 29_Cupones (specs/nv-29). */
@Composable
fun CouponsRoute(
    onBack: () -> Unit,
    onOpen: (WalletCoupon) -> Unit,
    onExplore: () -> Unit,
    viewModel: CouponsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Scaffold(snackbarHost = { WhereverSnackbarHost(snackbar) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            CouponsContent(
                state,
                CouponsActions(
                    onBack = onBack,
                    onTab = viewModel::onTab,
                    onOpen = onOpen,
                    onExplore = onExplore,
                    onLoadMore = viewModel::loadMore,
                    onRetry = viewModel::load
                )
            )
        }
    }
}

@Composable
internal fun CouponsContent(state: CouponsUiState, actions: CouponsActions) {
    val error = state.error
    FormScreen {
        TopTitleRow(stringResource(R.string.coupons_wallet_title), actions.onBack)
        Text(
            stringResource(R.string.coupons_wallet_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        SegmentedControl(
            options = listOf(
                state.availableCount?.let { stringResource(R.string.coupons_tab_available_count, it) }
                    ?: stringResource(R.string.coupons_tab_available),
                stringResource(R.string.coupons_tab_used)
            ),
            selectedIndex = state.tab.ordinal,
            onSelect = { actions.onTab(CouponsTab.entries[it]) }
        )
        when {
            state.loading -> ListSkeleton(rows = 4)
            error != null -> ErrorState(error.asString(), actions.onRetry)
            state.current.isEmpty() -> Empty(state.tab, actions.onExplore)
            else -> {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
                    val used = state.tab == CouponsTab.USED
                    state.current.forEach { card ->
                        CouponTicket(
                            coupon = card.coupon,
                            expiry = card.expiry,
                            used = used,
                            modifier = Modifier.testTag("coupon_${card.coupon.id}"),
                            action = if (used) {
                                null
                            } else {
                                { UseLink({ actions.onOpen(card.coupon) }, "coupon_use_${card.coupon.id}") }
                            }
                        )
                    }
                }
                if (state.cursor != null) {
                    WhereverTextLink(
                        stringResource(R.string.coupons_load_more),
                        actions.onLoadMore,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }
    }
}

@Composable
private fun Empty(tab: CouponsTab, onExplore: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S), modifier = Modifier.testTag("coupons_empty")) {
        Text(
            stringResource(if (tab == CouponsTab.USED) R.string.coupons_used_empty else R.string.coupons_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (tab == CouponsTab.AVAILABLE) WhereverTextLink(stringResource(R.string.coupons_explore), onExplore)
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun WalletCouponsPreview() {
    WhereverTheme { CouponsContent(previewCouponsState(), CouponsActions()) }
}

@Preview
@Composable
internal fun WalletCouponsEmptyPreview() {
    WhereverTheme {
        CouponsContent(
            CouponsUiState(loading = false, pages = mapOf(CouponsTab.AVAILABLE to CouponsPage(emptyList(), null))),
            CouponsActions()
        )
    }
}

@Preview
@Composable
internal fun WalletCouponsUsedPreview() {
    WhereverTheme { CouponsContent(previewCouponsState().copy(tab = CouponsTab.USED), CouponsActions()) }
}
