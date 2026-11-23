package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.Mist
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.Coupon
import com.wherever.mobile.feature.business.domain.CouponGroup
import com.wherever.mobile.feature.business.domain.CouponStatus

/** 53_NegCupones (specs/nv-37). Se vuelve a pedir al regresar de 54_NegCrear. */
@Composable
fun CouponsRoute(businessId: String, onCreate: () -> Unit, viewModel: CouponsViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(businessId) {
        viewModel.start(businessId)
        onPauseOrDispose { }
    }
    CouponsContent(
        state,
        CouponsActions(
            onTab = viewModel::selectTab,
            onToggle = viewModel::toggle,
            onRetry = viewModel::load,
            onCreate = onCreate,
            onMessageShown = viewModel::messageShown
        )
    )
}

@Composable
internal fun CouponsContent(state: CouponsUiState, actions: CouponsActions) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    val error = state.error
    FormScreen(
        footer = {
            WhereverSnackbarHost(snackbar)
            WhereverPrimaryButton(
                text = stringResource(R.string.coupons_create),
                onClick = actions.onCreate,
                icon = Icons.Outlined.Add,
                modifier = Modifier.fillMaxWidth(),
                tag = "coupons_create"
            )
        }
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Text(stringResource(R.string.coupons_title), style = MaterialTheme.typography.headlineLarge)
            Text(
                stringResource(R.string.coupons_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SegmentedControl(
            options = listOf(
                stringResource(R.string.coupons_tab_active, state.count(CouponGroup.ACTIVE)),
                stringResource(R.string.coupons_tab_scheduled),
                stringResource(R.string.coupons_tab_ended)
            ),
            selectedIndex = state.tab.ordinal,
            onSelect = { actions.onTab(CouponGroup.entries[it]) },
            modifier = Modifier.testTag("coupons_tabs")
        )
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null && state.coupons.isEmpty() ->
                ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            state.visible.isEmpty() -> Text(
                stringResource(
                    when (state.tab) {
                        CouponGroup.ACTIVE -> R.string.coupons_empty_active
                        CouponGroup.SCHEDULED -> R.string.coupons_empty_scheduled
                        CouponGroup.ENDED -> R.string.coupons_empty_ended
                    }
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.XXL).testTag("coupons_empty")
            )
            else -> state.visible.forEachIndexed { index, coupon -> CouponRow(coupon, index, state, actions) }
        }
    }
}

@Composable
private fun CouponRow(coupon: Coupon, index: Int, state: CouponsUiState, actions: CouponsActions) {
    CouponTicket(
        badge = couponBadge(coupon.type, coupon.value),
        color = ticketColor(index),
        title = coupon.title,
        subtitle =
        coupon.conditions ?: coupon.validUntil?.take(DATE_CHARS)?.let {
            stringResource(R.string.coupons_until, it)
        },
        modifier = Modifier.testTag("coupon_${coupon.id}"),
        footer = {
            Text(
                pluralStringResource(R.plurals.coupons_used, coupon.uses, coupon.uses),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = RouteBlue
            )
        },
        trailing = if (coupon.status.group == CouponGroup.ACTIVE) {
            {
                Switch(
                    checked = coupon.status == CouponStatus.ACTIVE,
                    onCheckedChange = { actions.onToggle(coupon) },
                    enabled = coupon.id !in state.toggling,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = Mint,
                        checkedThumbColor = Mist,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.testTag("coupon_switch_${coupon.id}")
                )
            }
        } else {
            null
        }
    )
}

/** «2026-10-31» de `vigencia_fin`. */
private const val DATE_CHARS = 10

@Preview(name = "53_NegCupones claro")
@Preview(name = "53_NegCupones oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CouponsPreview() {
    WhereverTheme { CouponsContent(CouponsUiState(loading = false, coupons = sampleCoupons()), CouponsActions()) }
}

@Preview(name = "53_NegCupones vacío")
@Composable
internal fun CouponsEmptyPreview() {
    WhereverTheme { CouponsContent(CouponsUiState(loading = false), CouponsActions()) }
}
