package com.wherever.mobile.feature.reservations.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.ui.formatTime
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** 55_Reservar (US-075, US-082). */
@Composable
fun ReserveRoute(onBack: () -> Unit, onCreated: (String) -> Unit, viewModel: ReserveViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val zone = remember { ZoneId.systemDefault() }
    LaunchedEffect(viewModel) { viewModel.created.collect(onCreated) }
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.reserve_submit),
                onClick = viewModel::submit,
                enabled = state.canSubmit,
                loading = state.submitting,
                tag = "reserve_submit"
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.reserve_title), onBack)
        if (state.placeName.isNotBlank()) {
            Text(state.placeName, style = MaterialTheme.typography.titleMedium)
        }
        Label(R.string.reserve_day)
        Days(state.dates, state.date, viewModel::onDate)
        Label(R.string.reserve_people)
        People(state.people, viewModel::onPeople)
        Label(R.string.reserve_time)
        val slotsError = state.slotsError
        when {
            state.loadingSlots -> LoadingState()
            slotsError != null -> {
                ErrorBanner(slotsError.asString())
                WhereverTextLink(stringResource(R.string.common_retry), viewModel::loadSlots)
            }
            state.slots.isEmpty() -> Text(
                stringResource(R.string.reserve_no_slots),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("reserve_no_slots")
            )
            else -> SlotGrid(state.slots, state.slot?.time, zone, viewModel::onSlot)
        }
        LabeledTextField(
            label = stringResource(R.string.reserve_note),
            value = state.note,
            onValueChange = viewModel::onNote,
            placeholder = stringResource(R.string.reserve_note_hint),
            supporting = stringResource(R.string.common_char_count, state.note.length, NOTE_MAX),
            singleLine = false,
            tag = "reserve_note"
        )
        state.slot?.let { slot ->
            SurfaceCard(modifier = Modifier.testTag("reserve_summary")) {
                Text(
                    stringResource(
                        R.string.reserve_summary,
                        state.date?.format(DAY_FORMAT).orEmpty(),
                        formatTime(slot.time, zone),
                        pluralStringResource(R.plurals.reserve_people_count, state.people, state.people)
                    ),
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

private val DAY_FORMAT: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE d MMM", Locale.forLanguageTag("es-MX"))

@Composable
private fun Label(text: Int) {
    Text(
        stringResource(text),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun Days(dates: List<LocalDate>, selected: LocalDate?, onSelect: (LocalDate) -> Unit) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.testTag("reserve_days")) {
        items(dates) { date ->
            SelectChip(
                text = when (date) {
                    dates.first() -> stringResource(R.string.reserve_today)
                    dates.getOrNull(1) -> stringResource(R.string.reserve_tomorrow)
                    else -> date.format(DAY_FORMAT)
                },
                selected = date == selected,
                onClick = { onSelect(date) },
                tag = "day_$date"
            )
        }
    }
}

@Composable
private fun People(people: Int, onChange: (Int) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            pluralStringResource(R.plurals.reserve_people_count, people, people),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.weight(1f).testTag("reserve_people")
        )
        if (people > 1) {
            WhereverTextLink(stringResource(R.string.reserve_people_less), { onChange(people - 1) })
        }
        if (people < MAX_PEOPLE_RESERVE) {
            WhereverTextLink(stringResource(R.string.reserve_people_more), { onChange(people + 1) })
        }
    }
}
