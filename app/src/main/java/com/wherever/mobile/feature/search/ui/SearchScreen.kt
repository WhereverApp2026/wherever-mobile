package com.wherever.mobile.feature.search.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ChipGroup
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.NavRow
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.feature.friends.domain.Person
import com.wherever.mobile.feature.friends.ui.PersonRow
import com.wherever.mobile.feature.places.ui.PlaceCardItem

/** 19_Buscar: Lugares, Personas y Planes; recientes, ideas y "sin resultados" con la IA. */
@Composable
fun SearchRoute(actions: SearchActions, viewModel: SearchViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    SearchContent(state, viewModel, actions)
}

@Composable
private fun SearchContent(state: SearchUiState, viewModel: SearchViewModel, actions: SearchActions) {
    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).safeDrawingPadding()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp)
        ) {
            LabeledTextField(
                label = stringResource(R.string.search_title),
                value = state.query,
                onValueChange = viewModel::onQueryChange,
                placeholder = stringResource(R.string.search_placeholder),
                leadingIcon = Icons.Filled.Search,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                modifier = Modifier.weight(1f),
                tag = "search_field"
            )
            WhereverTextLink(
                stringResource(R.string.common_cancel),
                actions.onBack,
                Modifier.padding(start = 8.dp, top = 24.dp)
            )
        }
        SegmentedControl(
            options = listOf(
                stringResource(R.string.search_tab_places),
                stringResource(R.string.search_tab_people),
                stringResource(R.string.search_tab_plans)
            ),
            selectedIndex = state.tab.ordinal,
            onSelect = { viewModel.onTab(SearchTab.entries[it]) },
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )
        state.error?.let {
            ErrorBanner(it.asString(), Modifier.padding(horizontal = ScreenPadding, vertical = 8.dp))
        }
        LazyColumn(
            contentPadding = PaddingValues(ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.testTag("search_list")
        ) {
            when {
                !state.hasQuery -> startItems(state, viewModel, actions)
                state.peopleQueryInvalid -> item {
                    Text(
                        stringResource(R.string.search_people_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("search_people_hint")
                    )
                }
                state.loading -> item { LoadingState() }
                state.emptyResults -> item { NoResults(state.query.trim(), state.tab, actions) }
                else -> results(state, viewModel, actions)
            }
        }
    }
}

private val IDEAS = listOf(
    R.string.search_idea_tacos,
    R.string.search_idea_coffee,
    R.string.search_idea_date,
    R.string.search_idea_kids
)

private fun LazyListScope.startItems(state: SearchUiState, viewModel: SearchViewModel, actions: SearchActions) {
    if (state.recents.isNotEmpty()) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SectionTitle(R.string.search_recent, Modifier.weight(1f))
                WhereverTextLink(stringResource(R.string.search_recent_clear), viewModel::clearRecents)
            }
        }
        item {
            ChipGroup {
                state.recents.forEach { text ->
                    SelectChip(text, selected = false, onClick = { viewModel.useRecent(text) })
                }
            }
        }
    }
    item { SectionTitle(R.string.search_ideas) }
    item {
        ChipGroup {
            IDEAS.forEach { res ->
                val text = stringResource(res)
                SelectChip(text, selected = false, onClick = { viewModel.useRecent(text) })
            }
        }
    }
    item {
        SurfaceCard { NavRow(stringResource(R.string.search_nearby), actions.onExplore, tag = "search_nearby") }
    }
}

private fun LazyListScope.results(state: SearchUiState, viewModel: SearchViewModel, actions: SearchActions) {
    // La barra de Inicio antes abría la IA directo (nv-48): la primera fila lo conserva.
    item {
        SurfaceCard {
            NavRow(
                stringResource(R.string.search_plan_with_ai, state.query.trim()),
                { actions.onAskAi(state.query.trim()) },
                tag = "search_plan_ai"
            )
        }
    }
    when (state.tab) {
        SearchTab.PLACES -> items(state.places, key = { it.id }) { place ->
            PlaceCardItem(place, onClick = { actions.onPlace(place.id) })
        }
        SearchTab.PEOPLE -> items(state.people, key = { it.id }) { person ->
            SearchPerson(person, state, viewModel, actions.onPerson)
        }
        SearchTab.PLANS -> items(state.plans, key = { it.id }) { plan ->
            SurfaceCard(modifier = Modifier.testTag("search_plan_${plan.id}")) {
                NavRow(plan.name, { actions.onPlan(plan.id) })
            }
        }
    }
}

/** 20_BuscarPersonas y su variante: Agregar pasa a Pendiente. */
@Composable
private fun SearchPerson(person: Person, state: SearchUiState, viewModel: SearchViewModel, onPerson: (String) -> Unit) {
    val sent = person.id in state.sent
    PersonRow(
        person,
        onClick = { onPerson(person.id) },
        detail = if (sent) stringResource(R.string.friends_request_pending) else null
    ) {
        if (!sent) {
            IconButton(
                onClick = { viewModel.request(person) },
                enabled = person.id !in state.sending,
                modifier = Modifier.testTag("search_request_${person.id}")
            ) {
                Icon(Icons.Filled.Add, contentDescription = stringResource(R.string.friends_add_person), tint = Sunset)
            }
        }
    }
}

/** 21_SinResultados: no se encontró, ideas y pedirle a la IA algo parecido. */
@Composable
private fun NoResults(query: String, tab: SearchTab, actions: SearchActions) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth().testTag("search_empty")
    ) {
        Text(stringResource(R.string.search_no_results, query), style = MaterialTheme.typography.titleMedium)
        Text(
            stringResource(R.string.search_no_results_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        WhereverPrimaryButton(
            text = stringResource(R.string.search_ask_ai),
            onClick = { actions.onAskAi(query) },
            tag = "search_ask_ai"
        )
        if (tab == SearchTab.PLACES) {
            WhereverOutlinedButton(
                text = stringResource(R.string.search_maybe),
                onClick = actions.onExplore,
                modifier = Modifier.fillMaxWidth(),
                tag = "search_explore"
            )
        }
    }
}

@Composable
private fun SectionTitle(@StringRes text: Int, modifier: Modifier = Modifier) {
    Text(stringResource(text), style = MaterialTheme.typography.titleMedium, modifier = modifier)
}
