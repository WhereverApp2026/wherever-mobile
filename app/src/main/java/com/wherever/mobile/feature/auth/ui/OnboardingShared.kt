package com.wherever.mobile.feature.auth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R

@Suppress("FunctionNaming")
@Composable
fun OnboardingHeader(step: Int, title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.onboarding_step_indicator, step),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary
        )
        Text(text = title, style = MaterialTheme.typography.headlineLarge)
        Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
    }
}

@Suppress("FunctionNaming")
@Composable
fun OnboardingSectionLabel(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleMedium)
}

@Suppress("FunctionNaming")
@Composable
fun SelectableChip(text: String, selected: Boolean, onClick: () -> Unit, testTag: String) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = text) },
        modifier = Modifier
            .padding(end = 8.dp, bottom = 8.dp)
            .testTag(testTag)
    )
}

@Suppress("FunctionNaming")
@Composable
fun OptionChip(id: String, selected: String, group: String, onSelect: (String) -> Unit) {
    SelectableChip(
        text = onboardingOptionLabel(id),
        selected = id == selected,
        onClick = { onSelect(id) },
        testTag = "onboarding_${group}_$id"
    )
}

@Suppress("FunctionNaming")
@Composable
fun OptionGrid(group: String, options: List<String>, selected: String, onSelect: (String) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        options.chunked(2).forEach { row ->
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                row.forEach { id ->
                    OptionChip(id = id, selected = selected, group = group, onSelect = onSelect)
                }
            }
        }
    }
}

@Composable
fun onboardingOptionLabel(id: String): String {
    val resource = OPTION_LABELS[id]
    return if (resource == null) id else stringResource(resource)
}

private val OPTION_LABELS = mapOf(
    "cafe" to R.string.onboarding_interest_cafe,
    "comida" to R.string.onboarding_interest_comida,
    "cultura" to R.string.onboarding_interest_cultura,
    "musica" to R.string.onboarding_interest_musica,
    "aire_libre" to R.string.onboarding_interest_aire_libre,
    "deporte" to R.string.onboarding_interest_deporte,
    "viajes" to R.string.onboarding_interest_viajes,
    "bienestar" to R.string.onboarding_interest_bienestar,
    "noche" to R.string.onboarding_interest_noche,
    "compras" to R.string.onboarding_interest_compras,
    "tranquilo" to R.string.onboarding_vibe_tranquilo,
    "social" to R.string.onboarding_vibe_social,
    "aventura" to R.string.onboarding_vibe_aventura,
    "cultural" to R.string.onboarding_vibe_cultural,
    "economico" to R.string.onboarding_budget_economico,
    "medio" to R.string.onboarding_budget_medio,
    "premium" to R.string.onboarding_budget_premium,
    "solo" to R.string.onboarding_company_solo,
    "pareja" to R.string.onboarding_company_pareja,
    "amigos" to R.string.onboarding_company_amigos,
    "familia" to R.string.onboarding_company_familia
)
