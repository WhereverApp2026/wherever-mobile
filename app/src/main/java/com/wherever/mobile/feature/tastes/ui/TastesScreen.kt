package com.wherever.mobile.feature.tastes.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.FieldErrorText
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.LabeledCheckbox
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.StepHeader
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.auth.ui.ONBOARDING_STEPS
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.ui.CityField

private data class TasteOption(val key: String, @param:StringRes val label: Int)

/** La API no publica un catálogo de claves (duda D05): se usan las de su ejemplo y otras con su forma. */
private val CATEGORIES = listOf(
    TasteOption("tacos", R.string.taste_tacos),
    TasteOption("cafes", R.string.taste_cafes),
    TasteOption("postres", R.string.taste_postres),
    TasteOption("aire_libre", R.string.taste_aire_libre),
    TasteOption("museos", R.string.taste_museos),
    TasteOption("musica", R.string.taste_musica),
    TasteOption("mercados", R.string.taste_mercados),
    TasteOption("bares", R.string.taste_bares),
    TasteOption("parques", R.string.taste_parques),
    TasteOption("comida_corrida", R.string.taste_comida_corrida),
    TasteOption("mariscos", R.string.taste_mariscos),
    TasteOption("arte", R.string.taste_arte),
    TasteOption("senderismo", R.string.taste_senderismo),
    TasteOption("cine", R.string.taste_cine),
    TasteOption("pueblos", R.string.taste_pueblos)
)

/** Etiqueta de una clave de categoría; null si no está en el catálogo local. */
@StringRes
internal fun tasteLabelRes(key: String): Int? = CATEGORIES.firstOrNull { it.key == key }?.label

private val MOBILITY = listOf(
    TasteOption("a_pie", R.string.mobility_a_pie),
    TasteOption("auto", R.string.mobility_auto),
    TasteOption("transporte_publico", R.string.mobility_transporte_publico),
    TasteOption("bicicleta", R.string.mobility_bicicleta)
)

private val RESTRICTIONS = listOf(
    TasteOption("vegetariano", R.string.restriction_vegetariano),
    TasteOption("vegano", R.string.restriction_vegano),
    TasteOption("sin_gluten", R.string.restriction_sin_gluten),
    TasteOption("sin_lactosa", R.string.restriction_sin_lactosa),
    TasteOption("accesible", R.string.restriction_accesible)
)

/** 06_Gustos */
@Composable
fun TastesRoute(onBack: () -> Unit, onFinished: () -> Unit, viewModel: TastesViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.finished.collect { onFinished() } }
    TastesContent(
        state = state,
        actions = TastesActions(
            onCategory = viewModel::toggleCategory,
            onRestriction = viewModel::toggleRestriction,
            budget = BudgetActions(
                onMin = viewModel::onBudgetMinChange,
                onMax = viewModel::onBudgetMaxChange,
                onSlide = viewModel::onBudgetSlide,
                onSlideFinished = viewModel::onBudgetSlideFinished
            ),
            onMobility = viewModel::onMobilityChange,
            onAllergiesEnabled = viewModel::onAllergiesEnabled,
            onAllergies = viewModel::onAllergiesChange,
            onAllergiesConsent = viewModel::onAllergiesConsentChange,
            onCityQuery = viewModel::onCityQueryChange,
            onCitySelect = viewModel::onCitySelect,
            onSubmit = viewModel::submit,
            onSkip = viewModel::skip,
            onBack = onBack
        )
    )
}

private data class TastesActions(
    val onCategory: (String) -> Unit,
    val onRestriction: (String) -> Unit,
    val budget: BudgetActions,
    val onMobility: (String) -> Unit,
    val onAllergiesEnabled: (Boolean) -> Unit,
    val onAllergies: (String) -> Unit,
    val onAllergiesConsent: (Boolean) -> Unit,
    val onCityQuery: (String) -> Unit,
    val onCitySelect: (CitySuggestion) -> Unit,
    val onSubmit: () -> Unit,
    val onSkip: () -> Unit,
    val onBack: () -> Unit
)

@Composable
private fun TastesContent(state: TastesUiState, actions: TastesActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(if (state.fromSettings) R.string.common_save else R.string.common_continue),
                onClick = actions.onSubmit,
                enabled = state.canSubmit,
                loading = state.saving,
                tag = "tastes_submit"
            )
            if (!state.fromSettings) WhereverTextLink(stringResource(R.string.tastes_skip), actions.onSkip)
        }
    ) {
        if (state.fromSettings) {
            TopTitleRow(stringResource(R.string.tastes_settings_title), actions.onBack)
        } else {
            StepHeader(stringResource(R.string.tastes_step), step = 2, total = ONBOARDING_STEPS, onBack = null)
            TitleBlock(stringResource(R.string.tastes_title), subtitle = stringResource(R.string.tastes_subtitle))
        }
        Section(stringResource(R.string.tastes_section_categories, state.categories.size, TASTES_MIN)) {
            OptionChips(CATEGORIES, state.categories, actions.onCategory, "tastes_category_")
        }
        Section(stringResource(R.string.tastes_section_mobility)) {
            OptionChips(MOBILITY, setOfNotNull(state.mobility), actions.onMobility, "tastes_mobility_")
        }
        Section(stringResource(R.string.tastes_section_budget)) {
            BudgetSection(
                form = state.budget,
                actions = actions.budget,
                minApiError = state.budgetMinError?.asString(),
                maxApiError = state.budgetMaxError?.asString()
            )
        }
        Section(stringResource(R.string.tastes_section_restrictions)) {
            ChipGroup {
                RESTRICTIONS.forEach { option ->
                    SelectChip(
                        stringResource(option.label),
                        option.key in state.restrictions,
                        { actions.onRestriction(option.key) }
                    )
                }
                SelectChip(
                    stringResource(R.string.tastes_allergies_chip),
                    state.allergiesEnabled,
                    { actions.onAllergiesEnabled(!state.allergiesEnabled) },
                    tag = "tastes_allergies_chip"
                )
            }
        }
        if (state.allergiesEnabled) {
            LabeledTextField(
                label = stringResource(R.string.tastes_allergies_label),
                value = state.allergies,
                onValueChange = actions.onAllergies,
                placeholder = stringResource(R.string.tastes_allergies_placeholder),
                supporting = stringResource(R.string.common_char_count, state.allergies.length, ALLERGIES_MAX),
                tag = "tastes_allergies"
            )
            AllergiesConsent(state, actions.onAllergiesConsent)
        }
        CityField(state.city, actions.onCityQuery, actions.onCitySelect, tag = "tastes_city")
        state.error?.let { ErrorBanner(it.asString()) }
    }
}

/** Consentimiento expreso para un dato de salud (nv-43): casilla aparte, nunca implícita. */
@Composable
private fun AllergiesConsent(state: TastesUiState, onChange: (Boolean) -> Unit) {
    Column {
        LabeledCheckbox(state.allergiesConsent, onChange, tag = "tastes_allergies_consent") {
            Text(
                text = stringResource(R.string.tastes_allergies_consent),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
        FieldErrorText(
            error = state.consentError?.asString(),
            modifier = Modifier.testTag("tastes_allergies_consent_error")
        )
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onBackground)
        content()
    }
}

@Composable
private fun OptionChips(
    options: List<TasteOption>,
    selected: Set<String>,
    onToggle: (String) -> Unit,
    tagPrefix: String
) {
    ChipGroup {
        options.forEach { option ->
            SelectChip(
                text = stringResource(option.label),
                selected = option.key in selected,
                onClick = { onToggle(option.key) },
                tag = tagPrefix + option.key
            )
        }
    }
}

@Preview
@Composable
private fun TastesPreview() {
    WhereverTheme(darkTheme = false) {
        TastesContent(
            TastesUiState(categories = setOf("tacos", "cafes")),
            TastesActions({}, {}, BudgetActions({}, {}, { _, _ -> }, {}), {}, {}, {}, {}, {}, {}, {}, {}, {})
        )
    }
}
