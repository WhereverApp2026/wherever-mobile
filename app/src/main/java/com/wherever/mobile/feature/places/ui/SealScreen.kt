package com.wherever.mobile.feature.places.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.TitleBlock
import com.wherever.mobile.core.designsystem.TopTitleRow
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.places.domain.PlaceCard

@Composable
fun SealRoute(
    onBack: () -> Unit,
    onPlace: (String) -> Unit,
    onSeeAll: () -> Unit,
    viewModel: SealViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SealContent(state, SealActions(onBack, onPlace, onSeeAll, viewModel::load))
}

data class SealActions(
    val onBack: () -> Unit = {},
    val onPlace: (String) -> Unit = {},
    val onSeeAll: () -> Unit = {},
    val onRetry: () -> Unit = {}
)

private val RULES = listOf(
    R.string.seal_rule_age,
    R.string.seal_rule_reviews,
    R.string.seal_rule_rating,
    R.string.seal_rule_clean
)

@Composable
internal fun SealContent(state: SealUiState, actions: SealActions) {
    FormScreen(
        footer = {
            WhereverPrimaryButton(
                text = stringResource(R.string.seal_see_all),
                onClick = actions.onSeeAll,
                tag = "seal_see_all"
            )
        }
    ) {
        TopTitleRow(stringResource(R.string.seal_title), actions.onBack)
        Icon(
            Icons.Outlined.Star,
            contentDescription = null,
            tint = Gold,
            modifier = Modifier.padding(top = Spacing.S).size(SealIconSize).testTag("seal_badge")
        )
        TitleBlock(stringResource(R.string.seal_headline), subtitle = stringResource(R.string.seal_body))
        SurfaceCard {
            Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
                Text(stringResource(R.string.seal_requirements), style = MaterialTheme.typography.titleMedium)
                RULES.forEach { rule ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.M)
                    ) {
                        Icon(
                            Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(end = Spacing.XS)
                        )
                        Text(stringResource(rule), style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
        Text(stringResource(R.string.seal_nearby), style = MaterialTheme.typography.titleLarge)
        when {
            state.loading -> PlaceCardSkeleton()
            state.error != null -> ErrorState(state.error.asString(), actions.onRetry)
            state.places.isEmpty() -> Text(
                stringResource(R.string.seal_nearby_empty),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("seal_empty")
            )
            else -> state.places.forEach { place -> PlaceCardItem(place, onClick = { actions.onPlace(place.id) }) }
        }
    }
}

private val SealIconSize = Sizes.IconXXL

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun SealPreview() {
    WhereverTheme {
        SealContent(
            SealUiState(
                loading = false,
                places = listOf(
                    PlaceCard(
                        id = "p1",
                        name = "Fonda Doña Meche",
                        category = "restaurante",
                        priceLevel = 2,
                        averagePrice = 180,
                        rating = 4.8,
                        reviews = 42,
                        coverUrl = null,
                        distanceMeters = 650.0,
                        openNow = true,
                        seal = true
                    )
                )
            ),
            SealActions()
        )
    }
}

/** 34_Sello sin negocios con Sello cerca (staging hoy). */
@Preview
@Composable
internal fun SealEmptyPreview() {
    WhereverTheme { SealContent(SealUiState(loading = false), SealActions()) }
}
