package com.wherever.mobile.feature.profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wherever.mobile.R
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.designsystem.LabeledTextField
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.ui.UiText
import com.wherever.mobile.core.ui.toUiText
import com.wherever.mobile.feature.profile.domain.CitySuggestion
import com.wherever.mobile.feature.profile.domain.ProfileRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MIN_QUERY = 2
private const val DEBOUNCE_MILLIS = 300L

data class CitySearchState(
    val query: String = "",
    val selectedId: String? = null,
    val suggestions: List<CitySuggestion> = emptyList(),
    val noResults: Boolean = false,
    val error: UiText? = null
)

/** Autocompletado de ciudad, compartido por Gustos y Editar perfil. */
class CitySearchController(
    private val scope: CoroutineScope,
    private val repository: ProfileRepository,
    private val update: ((CitySearchState) -> CitySearchState) -> Unit
) {
    private var job: Job? = null

    fun onQueryChange(query: String) {
        job?.cancel()
        update { it.copy(query = query, selectedId = null, suggestions = emptyList(), noResults = false, error = null) }
        if (query.trim().length < MIN_QUERY) return
        job = scope.launch {
            delay(DEBOUNCE_MILLIS)
            when (val result = repository.searchCities(query.trim())) {
                is Result.Success -> update {
                    it.copy(suggestions = result.data, noResults = result.data.isEmpty())
                }
                is Result.Failure -> update { it.copy(error = result.error.toUiText()) }
            }
        }
    }

    fun onSelect(city: CitySuggestion) {
        job?.cancel()
        update { it.copy(query = city.name, selectedId = city.id, suggestions = emptyList(), noResults = false) }
    }
}

@Composable
fun CityField(
    state: CitySearchState,
    onQueryChange: (String) -> Unit,
    onSelect: (CitySuggestion) -> Unit,
    modifier: Modifier = Modifier,
    tag: String = ""
) {
    Column(modifier = modifier.fillMaxWidth()) {
        LabeledTextField(
            label = stringResource(R.string.field_city),
            value = state.query,
            onValueChange = onQueryChange,
            placeholder = stringResource(R.string.city_placeholder),
            leadingIcon = Icons.Filled.LocationOn,
            error = state.error?.asString(),
            supporting = if (state.noResults) stringResource(R.string.city_no_results) else null,
            tag = tag
        )
        if (state.suggestions.isNotEmpty()) {
            SurfaceCard(modifier = Modifier.padding(top = 6.dp)) {
                state.suggestions.forEach { city ->
                    Text(
                        text = stringResource(R.string.city_suggestion, city.name, city.state),
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .clickable { onSelect(city) }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}
