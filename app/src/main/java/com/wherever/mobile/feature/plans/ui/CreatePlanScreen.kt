package com.wherever.mobile.feature.plans.ui

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LocalMotionEnabled
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.plans.domain.Mobility
import kotlinx.coroutines.delay

private const val STEP_MILLIS = 1_400L
private const val PULSE_MILLIS = 700
private const val PULSE_MIN = 0.35f

/** Topes de presupuesto por persona de la hoja rápida, en pesos. */
@Suppress("MagicNumber")
private val BUDGETS = listOf(200, 400, 700, 1000)

/** Palabras que bastan para pedir un plan (US-035). */
private val QUICK_WORDS = listOf(
    R.string.quick_craving,
    R.string.quick_relax,
    R.string.quick_date,
    R.string.quick_friends,
    R.string.quick_family,
    R.string.quick_cheap
)

private val GENERATING_STEPS = listOf(R.string.generating_1, R.string.generating_2, R.string.generating_3)

/** 15_Asistente y 15b_HojaRapida. */
@Composable
fun CreatePlanRoute(onBack: () -> Unit, onSaved: (String) -> Unit, viewModel: CreatePlanViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.saved.collect(onSaved) }
    when (state.phase) {
        CreatePhase.GENERATING -> Generating(onCancel = viewModel::cancel)
        CreatePhase.RESULT -> DraftResult(state, viewModel, onBack)
        CreatePhase.EDITING -> Editor(state, viewModel, onBack)
    }
}

@Composable
private fun Editor(state: CreatePlanUiState, viewModel: CreatePlanViewModel, onBack: () -> Unit) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.plan_generate),
                onClick = viewModel::generate,
                enabled = state.canGenerate,
                tag = "plan_generate"
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.plan_create_title), onBack)
        LabeledTextField(
            label = stringResource(R.string.plan_phrase_label),
            value = state.phrase,
            onValueChange = viewModel::onPhrase,
            placeholder = stringResource(R.string.plan_phrase_hint),
            singleLine = false,
            supporting = stringResource(R.string.common_char_count, state.phrase.length, PHRASE_MAX),
            tag = "plan_phrase"
        )
        ChipGroup {
            QUICK_WORDS.forEach { word ->
                val text = stringResource(word)
                SelectChip(text, state.phrase == text, { viewModel.onQuickWord(text) })
            }
        }
        if (state.noLocation) InfoBanner(stringResource(R.string.plan_needs_location), tag = "plan_no_location")
        state.error?.let { ErrorBanner(it.asString()) }
        QuickSheet(state, viewModel)
    }
}

/** US-045, US-048, US-049, US-050: hoja rápida. */
@Composable
private fun QuickSheet(state: CreatePlanUiState, viewModel: CreatePlanViewModel) {
    SurfaceCard {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(R.string.plan_when), style = MaterialTheme.typography.titleSmall)
            ChipGroup {
                PlanWhen.entries.forEach { option ->
                    SelectChip(stringResource(whenLabel(option)), state.whenOption == option, {
                        viewModel.onWhen(option)
                    })
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    stringResource(R.string.plan_people, state.people),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f)
                )
                WhereverTextLink(stringResource(R.string.plan_people_less), { viewModel.onPeople(state.people - 1) })
                IconButton(onClick = {
                    viewModel.onPeople(state.people + 1)
                }, modifier = Modifier.testTag("plan_people_more")) {
                    Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.plan_people_more))
                }
            }
            Text(stringResource(R.string.plan_budget), style = MaterialTheme.typography.titleSmall)
            ChipGroup {
                BUDGETS.forEach { budget ->
                    SelectChip(
                        stringResource(R.string.plan_budget_up_to, budget),
                        state.budget == budget,
                        { viewModel.onBudget(budget) },
                        tag = "plan_budget_$budget"
                    )
                }
            }
            Text(stringResource(R.string.plan_mobility), style = MaterialTheme.typography.titleSmall)
            ChipGroup {
                Mobility.entries.forEach { mobility ->
                    SelectChip(stringResource(mobilityLabel(mobility)), state.mobility == mobility, {
                        viewModel.onMobility(mobility)
                    })
                }
            }
            Text(stringResource(R.string.plan_mood), style = MaterialTheme.typography.titleSmall)
            if (state.moodAllowed) {
                ChipGroup {
                    Mood.entries.forEach { mood ->
                        SelectChip(stringResource(moodLabel(mood)), state.mood == mood, {
                            viewModel.onMood(mood)
                        }, tag = "mood_${mood.name}")
                    }
                }
            } else {
                Text(
                    stringResource(R.string.plan_mood_consent),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** US-040: animación "Armando tu plan" con pasos que cambian; con animaciones apagadas queda fija. */
@Composable
private fun Generating(onCancel: () -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    val motion = LocalMotionEnabled.current
    LaunchedEffect(Unit) {
        while (true) {
            delay(STEP_MILLIS)
            step = (step + 1) % GENERATING_STEPS.size
        }
    }
    val pulse = rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = PULSE_MIN,
        animationSpec = infiniteRepeatable(tween(PULSE_MILLIS), RepeatMode.Reverse),
        label = "pulse"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        modifier = Modifier.fillMaxSize().padding(32.dp).testTag("plan_generating")
    ) {
        CircularProgressIndicator(color = RouteBlue, modifier = Modifier.size(56.dp))
        Text(
            stringResource(R.string.generating_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Text(
            stringResource(GENERATING_STEPS[step]),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .alpha(if (motion) pulse.value else 1f)
                .semantics { liveRegion = LiveRegionMode.Polite }
        )
        WhereverOutlinedButton(stringResource(R.string.common_cancel), onCancel, tag = "plan_cancel")
    }
}

@Composable
private fun DraftResult(state: CreatePlanUiState, viewModel: CreatePlanViewModel, onBack: () -> Unit) {
    val draft = state.draft ?: return
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.plan_save),
                onClick = viewModel::save,
                loading = state.saving,
                tag = "plan_save"
            )
            WhereverOutlinedButton(
                text = stringResource(R.string.plan_other_idea),
                onClick = viewModel::otherIdea,
                icon = Icons.Filled.Refresh,
                modifier = Modifier.fillMaxWidth(),
                tag = "plan_other_idea"
            )
        }
    ) {
        TopTitleRow(draft.name.ifBlank { stringResource(R.string.plan_create_title) }, onBack)
        if (draft.source == SOURCE_RULES) {
            InfoBanner(draft.notice ?: stringResource(R.string.plan_rules_notice), tag = "plan_rules")
        }
        draft.parameters?.let { params -> UnderstoodChips(params, draft.people) }
        state.error?.let { ErrorBanner(it.asString()) }
        CostSummary(draft.totalCost, draft.costPerPerson, draft.people)
        Timeline(stops = draft.stops, transfers = draft.transfers, mobility = draft.mobility)
        WhereverTextLink(stringResource(R.string.plan_adjust), viewModel::edit)
    }
}

internal const val SOURCE_RULES = "reglas"
