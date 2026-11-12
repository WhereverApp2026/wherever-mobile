package com.wherever.mobile.feature.tastes.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.feature.tastes.domain.BudgetIssue

/**
 * Gasto por salida (nv-44): barra doble y dos campos sincronizados. Toda la lógica está en `BudgetForm`
 * y en el ViewModel; aquí solo se pinta y se envían eventos. Los errores de la API tienen prioridad.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BudgetSection(
    form: BudgetForm,
    actions: BudgetActions,
    minApiError: String? = null,
    maxApiError: String? = null
) {
    val rangeLabel = rangeLabel(form)
    val sliderDescription = stringResource(R.string.tastes_budget_slider_description)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = rangeLabel,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(horizontal = 4.dp).testTag("tastes_budget_range")
        )
        RangeSlider(
            value = form.sliderStart..form.sliderEnd,
            onValueChange = { range -> actions.onSlide(range.start, range.endInclusive) },
            onValueChangeFinished = actions.onSlideFinished,
            valueRange = 0f..BUDGET_LIMIT.toFloat(),
            colors = SliderDefaults.colors(thumbColor = RouteBlue, activeTrackColor = RouteBlue),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .semantics {
                    contentDescription = sliderDescription
                    stateDescription = rangeLabel
                }
                .testTag("tastes_budget")
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AmountField(
                label = stringResource(R.string.tastes_budget_min),
                accessibilityLabel = stringResource(R.string.tastes_budget_min_description),
                value = form.minText,
                onValueChange = actions.onMin,
                error = minApiError ?: form.minIssue?.let { issueText(it) },
                imeAction = ImeAction.Next,
                tag = "tastes_budget_min",
                modifier = Modifier.weight(1f)
            )
            AmountField(
                label = stringResource(R.string.tastes_budget_max),
                accessibilityLabel = stringResource(R.string.tastes_budget_max_description),
                value = form.maxText,
                onValueChange = actions.onMax,
                error = maxApiError ?: form.maxIssue?.let { issueText(it) },
                imeAction = ImeAction.Done,
                tag = "tastes_budget_max",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AmountField(
    label: String,
    accessibilityLabel: String,
    value: String,
    onValueChange: (String) -> Unit,
    error: String?,
    imeAction: ImeAction,
    tag: String,
    modifier: Modifier = Modifier
) {
    LabeledTextField(
        label = label,
        value = value,
        onValueChange = onValueChange,
        error = error,
        prefix = stringResource(R.string.tastes_budget_prefix),
        accessibilityLabel = accessibilityLabel,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = imeAction),
        tag = tag,
        modifier = modifier
    )
}

@Composable
private fun rangeLabel(form: BudgetForm): String {
    val min = form.min?.toString() ?: PENDING
    val max = form.max
    return if (max == BUDGET_LIMIT) {
        stringResource(R.string.tastes_budget_range_open, min, max.toString())
    } else {
        stringResource(R.string.tastes_budget_range, min, max?.toString() ?: PENDING)
    }
}

@Composable
private fun issueText(issue: BudgetIssue): String = stringResource(
    when (issue) {
        BudgetIssue.EMPTY -> R.string.tastes_budget_empty
        BudgetIssue.OUT_OF_RANGE -> R.string.tastes_budget_out_of_range
        BudgetIssue.MIN_GREATER_THAN_MAX -> R.string.tastes_budget_min_greater
    }
)

private const val PENDING = "…"
