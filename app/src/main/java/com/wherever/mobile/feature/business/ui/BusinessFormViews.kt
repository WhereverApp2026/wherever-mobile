package com.wherever.mobile.feature.business.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.feature.auth.ui.toPickerDate
import com.wherever.mobile.feature.auth.ui.toPickerMillis
import java.time.LocalDate

/** Fila con título, ayuda y −/+ (límite de usos, cupo y duración del turno). */
@Composable
internal fun BusinessStepper(
    title: String,
    subtitle: String?,
    value: String,
    onLess: () -> Unit,
    onMore: () -> Unit,
    tag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.XS)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            subtitle?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        CircleIconButton(
            icon = ImageVector.vectorResource(R.drawable.ic_wh_minus),
            description = stringResource(R.string.business_less, title),
            onClick = onLess,
            modifier = Modifier.testTag("${tag}_less")
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = Spacing.M).testTag("${tag}_value")
        )
        CircleIconButton(
            icon = Icons.Outlined.Add,
            description = stringResource(R.string.business_more, title),
            onClick = onMore,
            modifier = Modifier.testTag("${tag}_more")
        )
    }
}

/** Calendario de un día entre [first] y [last]. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun BusinessDatePicker(
    first: LocalDate,
    last: LocalDate,
    initial: LocalDate,
    onDismiss: () -> Unit,
    onPicked: (LocalDate) -> Unit
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = initial.toPickerMillis(),
        yearRange = first.year..last.year,
        selectableDates = remember(first, last) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long) = utcTimeMillis.toPickerDate() in first..last

                override fun isSelectableYear(year: Int) = year in first.year..last.year
            }
        }
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            WhereverTextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = { pickerState.selectedDateMillis?.let { onPicked(it.toPickerDate()) } }
            ) { Text(stringResource(R.string.common_accept)) }
        },
        dismissButton = { WhereverTextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } }
    ) {
        DatePicker(state = pickerState)
    }
}
