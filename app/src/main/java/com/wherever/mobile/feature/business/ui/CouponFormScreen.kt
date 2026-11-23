package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.BackCircleButton
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.COUPON_TITLE_MAX
import com.wherever.mobile.feature.business.domain.CouponType
import com.wherever.mobile.feature.business.domain.DAYS_IN_WEEK
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle

/** Días como los pinta la maqueta (lunes primero); el número es el de la API (0 domingo a 6 sábado). */
private val WEEK = DayOfWeek.entries.map { it.value % DAYS_IN_WEEK }

/** La vista previa va en verde, como en la maqueta. */
private const val PREVIEW_COLOR = 2

/** Vigencia máxima que se puede elegir en el calendario. */
private const val MAX_VALIDITY_DAYS = 365L

/** 54_NegCrear (specs/nv-37). */
@Composable
fun CouponFormRoute(businessId: String, onBack: () -> Unit, viewModel: CouponFormViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(businessId) { viewModel.start(businessId) }
    LaunchedEffect(viewModel) { viewModel.published.collect { onBack() } }
    CouponFormContent(
        state,
        viewModel.today(),
        CouponFormActions(
            onBack = onBack,
            onType = viewModel::onType,
            onValue = viewModel::onValue,
            onTitle = viewModel::onTitle,
            onSuggest = viewModel::suggestTitle,
            onDay = viewModel::toggleDay,
            onHoursFrom = viewModel::onHoursFrom,
            onHoursTo = viewModel::onHoursTo,
            onEnd = viewModel::onEnd,
            onLimit = viewModel::onLimit,
            onPublish = viewModel::publish
        )
    )
}

@Composable
internal fun CouponFormContent(state: CouponFormUiState, today: LocalDate, actions: CouponFormActions) {
    var pickEnd by remember { mutableStateOf(false) }
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.coupon_publish),
                onClick = actions.onPublish,
                enabled = state.canPublish,
                loading = state.saving,
                modifier = Modifier.fillMaxWidth(),
                tag = "coupon_publish"
            )
        }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            BackCircleButton(onClick = actions.onBack)
            Spacer(Modifier.width(Spacing.M))
            Text(stringResource(R.string.coupon_new_title), style = MaterialTheme.typography.headlineMedium)
        }
        Label(R.string.coupon_type)
        ChipGroup {
            CouponType.entries.forEach { type ->
                SelectChip(
                    stringResource(typeLabel(type)),
                    state.type == type,
                    { actions.onType(type) },
                    tag = "coupon_type_${type.apiValue}"
                )
            }
        }
        if (state.type.hasValue) {
            LabeledTextField(
                label = stringResource(R.string.coupon_discount),
                value = state.value,
                onValueChange = actions.onValue,
                placeholder = stringResource(
                    if (state.type == CouponType.PERCENT) R.string.coupon_percent_hint else R.string.coupon_amount_hint
                ),
                prefix = stringResource(R.string.coupon_currency).takeIf { state.type == CouponType.AMOUNT },
                trailingContent = if (state.type == CouponType.PERCENT) {
                    { Text(stringResource(R.string.coupon_percent_sign)) }
                } else {
                    null
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                tag = "coupon_value"
            )
        }
        LabeledTextField(
            label = stringResource(R.string.coupon_name),
            value = state.title,
            onValueChange = actions.onTitle,
            placeholder = stringResource(R.string.coupon_name_hint),
            supporting = stringResource(R.string.common_char_count, state.title.length, COUPON_TITLE_MAX),
            tag = "coupon_title"
        )
        WhereverTextLink(
            stringResource(if (state.suggesting) R.string.coupon_suggesting else R.string.coupon_suggest),
            actions.onSuggest,
            Modifier.testTag("coupon_suggest"),
            enabled = !state.suggesting
        )
        Label(R.string.coupon_when)
        ChipGroup {
            WEEK.forEach { day ->
                SelectChip(
                    DayOfWeek.of(if (day == 0) DAYS_IN_WEEK else day).getDisplayName(TextStyle.SHORT, MexicanSpanish)
                        .replace(".", "").replaceFirstChar { it.uppercase() },
                    day in state.days,
                    { actions.onDay(day) },
                    tag = "coupon_day_$day"
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.M)) {
            LabeledTextField(
                label = stringResource(R.string.coupon_from),
                value = state.hoursFrom,
                onValueChange = actions.onHoursFrom,
                placeholder = stringResource(R.string.coupon_from_hint),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                tag = "coupon_from"
            )
            LabeledTextField(
                label = stringResource(R.string.coupon_to),
                value = state.hoursTo,
                onValueChange = actions.onHoursTo,
                placeholder = stringResource(R.string.coupon_to_hint),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                tag = "coupon_to"
            )
        }
        if (!state.hoursValid) ErrorBanner(stringResource(R.string.business_schedule_invalid))
        Label(R.string.coupon_until)
        Surface(
            onClick = { pickEnd = true },
            shape = RoundedCornerShape(Radius.Field),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(Strokes.Hairline, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth().heightIn(min = Sizes.FieldHeight).testTag("coupon_end")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = Spacing.L, vertical = Sizes.FieldVerticalPadding)
            ) {
                Icon(
                    Icons.Outlined.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(Spacing.M))
                Text(state.end?.shortText().orEmpty(), style = MaterialTheme.typography.bodyLarge)
            }
        }
        BusinessStepper(
            title = stringResource(R.string.coupon_limit),
            subtitle = stringResource(R.string.coupon_limit_hint),
            value = if (state.usageLimit ==
                0
            ) {
                stringResource(R.string.coupon_no_limit)
            } else {
                state.usageLimit.toString()
            },
            onLess = { actions.onLimit(state.usageLimit - USAGE_STEP) },
            onMore = { actions.onLimit(state.usageLimit + USAGE_STEP) },
            tag = "coupon_limit"
        )
        Label(R.string.coupon_preview)
        CouponTicket(
            badge = couponBadge(state.type, state.numericValue),
            color = ticketColor(PREVIEW_COLOR),
            title = state.title.ifBlank { stringResource(R.string.coupon_name_hint) },
            subtitle = state.end?.let { stringResource(R.string.coupons_until, it.shortText()) },
            modifier = Modifier.testTag("coupon_preview")
        )
        state.error?.let { ErrorBanner(it.asString()) }
    }
    if (pickEnd) {
        BusinessDatePicker(
            first = today.plusDays(1),
            last = today.plusDays(MAX_VALIDITY_DAYS),
            initial = state.end ?: today.plusDays(1),
            onDismiss = { pickEnd = false }
        ) {
            pickEnd = false
            actions.onEnd(it)
        }
    }
}

@Composable
private fun Label(text: Int) {
    Text(stringResource(text), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
}

internal fun typeLabel(type: CouponType): Int = when (type) {
    CouponType.TWO_FOR_ONE -> R.string.coupon_badge_2x1
    CouponType.PERCENT -> R.string.coupon_type_percent
    CouponType.AMOUNT -> R.string.coupon_type_amount
    CouponType.GIFT -> R.string.coupon_badge_gift
}

@Preview(name = "54_NegCrear claro")
@Preview(name = "54_NegCrear oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun CouponFormPreview() {
    WhereverTheme { CouponFormContent(sampleCouponForm(), SAMPLE_TODAY, CouponFormActions()) }
}
