package com.wherever.mobile.feature.plans.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverBottomBar
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.ui.formatDate
import com.wherever.mobile.feature.plans.domain.PlanSummary
import com.wherever.mobile.feature.plans.domain.PlansTab
import java.time.ZoneOffset

private val TABS = listOf(
    PlansTab.UPCOMING to R.string.plans_upcoming,
    PlansTab.SAVED to R.string.plans_saved,
    PlansTab.PAST to R.string.plans_past
)

/** 24_MisPlanes. Se recarga al volver del detalle. */
@Composable
fun MyPlansRoute(
    onTab: (MainTab) -> Unit,
    onPlan: (String) -> Unit,
    onCreate: () -> Unit,
    viewModel: MyPlansViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(viewModel) {
        viewModel.load()
        onPauseOrDispose { }
    }
    Scaffold(
        bottomBar = { WhereverBottomBar(MainTab.PLANS, onSelect = onTab) },
        floatingActionButton = {
            SmallFloatingActionButton(onClick = onCreate, modifier = Modifier.testTag("plans_create")) {
                Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.plan_create_title))
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .statusBarsPadding()
                .padding(bottom = padding.calculateBottomPadding())
        ) {
            Text(
                stringResource(R.string.plans_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(ScreenPadding)
            )
            SegmentedControl(
                options = TABS.map { stringResource(it.second) },
                selectedIndex = TABS.indexOfFirst { it.first == state.tab },
                onSelect = { viewModel.onTab(TABS[it].first) },
                modifier = Modifier.padding(horizontal = ScreenPadding)
            )
            when {
                state.loading -> ListSkeleton()
                state.error != null && state.items.isEmpty() ->
                    ErrorState(state.error?.asString().orEmpty(), onRetry = viewModel::load)
                state.items.isEmpty() -> Column(
                    modifier = Modifier.padding(ScreenPadding).testTag("plans_empty"),
                    verticalArrangement = Arrangement.spacedBy(Spacing.S)
                ) {
                    Text(stringResource(R.string.plans_empty), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    WhereverTextLink(stringResource(R.string.plan_create_title), onCreate)
                }
                else -> LazyColumn(
                    contentPadding = PaddingValues(ScreenPadding),
                    verticalArrangement = Arrangement.spacedBy(Spacing.M)
                ) {
                    items(state.items, key = { it.id }) { PlanRow(it) { onPlan(it.id) } }
                }
            }
        }
    }
}

@Composable
private fun PlanRow(plan: PlanSummary, onClick: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("plan_row_${plan.id}")) {
        Column(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(Spacing.L)) {
            Text(plan.name, style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    R.string.plans_row_detail,
                    formatDate("${plan.date}T12:00:00Z", ZoneOffset.UTC),
                    plan.stops,
                    plan.costPerPerson
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
