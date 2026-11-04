package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.components.WhereverPrimaryButton
import com.wherever.mobile.feature.auth.domain.OnboardingOptions
import com.wherever.mobile.feature.auth.ui.components.AuthErrorBanner

@Composable
fun PreferencesScreen(onContinue: () -> Unit, viewModel: PreferencesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) {
        viewModel.events.collect { onContinue() }
    }
    PreferencesContent(
        state = state,
        onToggleInterest = viewModel::toggleInterest,
        onSelectVibe = viewModel::selectVibe,
        onSelectBudget = viewModel::selectBudget,
        onSelectCompany = viewModel::selectCompany,
        onSave = viewModel::save,
        onSkip = viewModel::skip
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PreferencesContent(
    state: PreferencesUiState,
    onToggleInterest: (String) -> Unit,
    onSelectVibe: (String) -> Unit,
    onSelectBudget: (String) -> Unit,
    onSelectCompany: (String) -> Unit,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        OnboardingHeader(
            step = 1,
            title = stringResource(R.string.onboarding_preferences_title),
            subtitle = stringResource(R.string.onboarding_preferences_subtitle)
        )
        OnboardingSectionLabel(stringResource(R.string.onboarding_interests_label))
        FlowRow {
            OnboardingOptions.interests.forEach { id ->
                SelectableChip(
                    text = onboardingOptionLabel(id),
                    selected = id in state.interests,
                    onClick = { onToggleInterest(id) },
                    testTag = "onboarding_interest_$id"
                )
            }
        }
        if (state.interestsMissing) {
            Text(
                text = stringResource(R.string.onboarding_interests_missing),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        OnboardingSectionLabel(stringResource(R.string.onboarding_vibe_label))
        OptionGrid("vibe", OnboardingOptions.vibes, state.vibe, onSelectVibe)
        OnboardingSectionLabel(stringResource(R.string.onboarding_budget_label))
        OptionGrid("budget", OnboardingOptions.budgets, state.budget, onSelectBudget)
        OnboardingSectionLabel(stringResource(R.string.onboarding_company_label))
        OptionGrid("company", OnboardingOptions.companies, state.company, onSelectCompany)
        AuthErrorBanner(error = state.error)
        PreferencesActions(state = state, onSave = onSave, onSkip = onSkip)
    }
}

@Composable
private fun PreferencesActions(state: PreferencesUiState, onSave: () -> Unit, onSkip: () -> Unit) {
    WhereverPrimaryButton(
        text = if (state.isSaving) {
            stringResource(R.string.auth_action_loading)
        } else {
            stringResource(R.string.onboarding_action_continue)
        },
        onClick = onSave,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_preferences_continue"),
        enabled = !state.isLoading && !state.isSaving
    )
    TextButton(
        onClick = onSkip,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("onboarding_preferences_skip"),
        enabled = !state.isLoading && !state.isSaving
    ) {
        Text(text = stringResource(R.string.onboarding_action_skip))
    }
}
