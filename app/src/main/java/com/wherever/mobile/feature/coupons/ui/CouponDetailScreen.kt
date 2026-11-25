package com.wherever.mobile.feature.coupons.ui

import android.content.ClipData
import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.coupons.domain.Expiry
import com.wherever.mobile.feature.coupons.domain.WalletCoupon
import kotlinx.coroutines.launch

private const val SECONDS_PER_MINUTE = 60
private const val PREVIEW_DAYS_SOON = 3
private const val PREVIEW_DAYS_TWO = 2

data class CouponDetailActions(
    val onBack: () -> Unit = {},
    val onRedeem: () -> Unit = {},
    val onCopy: (String) -> Unit = {},
    val onReserve: (WalletCoupon) -> Unit = {},
    val onPlace: (WalletCoupon) -> Unit = {},
    val onRetry: () -> Unit = {}
)

/** 47_DetCupon y `DetCupon · *` (specs/nv-29). */
@Composable
fun CouponDetailRoute(
    onBack: () -> Unit,
    onReserve: (WalletCoupon) -> Unit,
    onPlace: (WalletCoupon) -> Unit,
    viewModel: CouponDetailViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    val copied = stringResource(R.string.coupons_code_copied)
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            viewModel.messageShown()
        }
    }
    Scaffold(snackbarHost = { WhereverSnackbarHost(snackbar) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).consumeWindowInsets(padding)) {
            CouponDetailContent(
                state,
                CouponDetailActions(
                    onBack = onBack,
                    onRedeem = viewModel::redeem,
                    onCopy = { code ->
                        scope.launch {
                            clipboard.setClipEntry(ClipEntry(ClipData.newPlainText(copied, code)))
                            snackbar.showSnackbar(copied)
                        }
                    },
                    onReserve = onReserve,
                    onPlace = onPlace,
                    onRetry = viewModel::load
                )
            )
        }
    }
}

@Composable
internal fun CouponDetailContent(state: CouponDetailUiState, actions: CouponDetailActions) {
    val coupon = state.coupon
    val error = state.error
    FormScreen(
        footer = coupon?.let {
            {
                WhereverPrimaryButton(
                    stringResource(R.string.coupons_reserve_with),
                    { actions.onReserve(it) },
                    tag = "coupon_reserve"
                )
                WhereverOutlinedButton(
                    stringResource(R.string.coupons_see_place, it.placeName),
                    { actions.onPlace(it) },
                    modifier = Modifier.fillMaxWidth(),
                    tag = "coupon_place"
                )
            }
        }
    ) {
        TopTitleRow(stringResource(R.string.coupons_detail_title), actions.onBack)
        when {
            state.loading -> ListSkeleton(rows = 3)
            error != null || coupon == null -> ErrorState(
                error?.asString() ?: stringResource(R.string.error_unknown),
                actions.onRetry
            )
            else -> {
                val used = coupon.state == USED_STATE
                CouponTicket(coupon, state.expiry, used = used)
                if (!used) CodeCard(state, actions)
                HowTo(coupon.placeName)
                state.conditions?.let { Conditions(it) }
            }
        }
    }
}

@Composable
private fun CodeCard(state: CouponDetailUiState, actions: CouponDetailActions) {
    val code = state.code
    SurfaceCard(modifier = Modifier.testTag("coupon_code_card")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier.fillMaxWidth().padding(Spacing.L)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    stringResource(R.string.coupons_code_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (code != null) {
                    Text(
                        code.code,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.testTag("coupon_code")
                    )
                    Text(
                        stringResource(
                            R.string.coupons_code_expires,
                            state.secondsLeft / SECONDS_PER_MINUTE,
                            state.secondsLeft % SECONDS_PER_MINUTE
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        stringResource(R.string.coupons_code_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            if (code != null) {
                WhereverOutlinedButton(stringResource(R.string.coupons_copy), {
                    actions.onCopy(code.code)
                }, tag = "coupon_copy")
            } else {
                WhereverOutlinedButton(
                    stringResource(R.string.coupons_show_code),
                    actions.onRedeem,
                    enabled = state.canRedeem && !state.redeeming,
                    tag = "coupon_redeem"
                )
            }
        }
    }
}

@Composable
private fun HowTo(placeName: String) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(stringResource(R.string.coupons_how_title), style = MaterialTheme.typography.titleSmall)
        listOf(
            stringResource(R.string.coupons_how_1, placeName),
            stringResource(R.string.coupons_how_2),
            stringResource(R.string.coupons_how_3)
        ).forEachIndexed { index, step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                Text(
                    (index + 1).toString(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .size(Sizes.IconL)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(top = Spacing.XS)
                )
                Text(step, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Conditions(text: String) {
    SurfaceCard(modifier = Modifier.testTag("coupon_conditions")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Text(stringResource(R.string.coupons_conditions), style = MaterialTheme.typography.titleSmall)
            Text(text, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CouponDetailPreview() {
    WhereverTheme {
        CouponDetailContent(
            previewDetailState(PreviewTwoForOne, Expiry.InDays(PREVIEW_DAYS_SOON)),
            CouponDetailActions()
        )
    }
}

@Preview
@Composable
internal fun CouponDetailCodePreview() {
    WhereverTheme {
        CouponDetailContent(
            previewDetailState(PreviewTwoForOne, Expiry.InDays(PREVIEW_DAYS_SOON), withCode = true),
            CouponDetailActions()
        )
    }
}

@Preview
@Composable
internal fun CouponDetailAmountPreview() {
    WhereverTheme { CouponDetailContent(previewDetailState(PreviewAmount, Expiry.Today), CouponDetailActions()) }
}

@Preview
@Composable
internal fun CouponDetailPercentPreview() {
    WhereverTheme {
        CouponDetailContent(previewDetailState(PreviewPercent, Expiry.InDays(PREVIEW_DAYS_TWO)), CouponDetailActions())
    }
}

@Preview
@Composable
internal fun CouponDetailGiftPreview() {
    WhereverTheme { CouponDetailContent(previewDetailState(PreviewGift, Expiry.InDays(1)), CouponDetailActions()) }
}
