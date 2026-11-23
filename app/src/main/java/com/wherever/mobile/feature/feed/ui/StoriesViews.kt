package com.wherever.mobile.feature.feed.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.feed.domain.StoryMoment
import com.wherever.mobile.feature.feed.domain.StoryPerson
import com.wherever.mobile.feature.profile.ui.ProfileAvatar
import kotlinx.coroutines.delay

/** Tiempo que dura cada historia en pantalla antes de pasar a la siguiente. */
private const val STORY_MILLIS = 5_000L

/** Fila de historias arriba del feed de Amigos: círculos con anillo azul → naranja (docs/diseno_boceto2.md §2.2). */
@Composable
fun StoriesRow(people: List<StoryPerson>, onOpen: (String) -> Unit, modifier: Modifier = Modifier) {
    if (people.isEmpty()) return
    LazyRow(
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        modifier = modifier.fillMaxWidth().testTag("stories_row")
    ) {
        items(people, key = StoryPerson::authorId) { person ->
            val name = person.username?.let { stringResource(R.string.friends_username_at, it) }.orEmpty()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.XS),
                modifier = Modifier
                    .width(Sizes.AvatarM + Spacing.M)
                    .clickable(role = Role.Button, onClickLabel = stringResource(R.string.stories_open, name)) {
                        onOpen(person.authorId)
                    }
                    .testTag("story_${person.authorId}")
            ) {
                Box(
                    modifier = Modifier
                        .border(Strokes.Thin, Brush.linearGradient(listOf(RouteBlue, Sunset)), CircleShape)
                        .padding(Strokes.Thick)
                ) {
                    ProfileAvatar(person.username ?: "?", person.photoUrl, Sizes.AvatarM)
                }
                Text(
                    person.username.orEmpty(),
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/** Visor de historias a pantalla completa. */
@Composable
fun StoryViewerRoute(
    onClose: () -> Unit,
    onMoment: (String) -> Unit,
    viewModel: StoryViewerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(state.finished) { if (state.finished) onClose() }
    // Avanza sola; cada cambio de historia reinicia la espera.
    LaunchedEffect(state.person, state.moment, state.loading) {
        if (!state.loading && state.currentMoment != null) {
            delay(STORY_MILLIS)
            viewModel.next()
        }
    }
    StoryViewerContent(state, onClose, onMoment, viewModel::previous, viewModel::next)
}

@Composable
internal fun StoryViewerContent(
    state: StoryViewerUiState,
    onClose: () -> Unit,
    onMoment: (String) -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    val person = state.current
    val moment = state.currentMoment
    when {
        state.loading -> LoadingState()
        state.error != null -> ErrorState(state.error.asString(), onClose)
        person == null || moment == null -> LoadingState()
        else -> Box(modifier = Modifier.fillMaxSize().background(Ink).testTag("story_viewer")) {
            AsyncImage(
                model = moment.imageUrl,
                contentDescription = moment.placeName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
            // Mitad izquierda = anterior, mitad derecha = siguiente.
            Row(modifier = Modifier.fillMaxSize()) {
                Box(Modifier.weight(1f).fillMaxSize().clickable(onClick = onPrevious).testTag("story_previous"))
                Box(Modifier.weight(1f).fillMaxSize().clickable(onClick = onNext).testTag("story_next"))
            }
            StoryHeader(person, state.moment, onClose)
            StoryFooter(moment, onMoment)
        }
    }
}

@Composable
private fun StoryHeader(person: StoryPerson, index: Int, onClose: () -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = Modifier.fillMaxWidth().safeDrawingPadding().padding(Spacing.M)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.XS), modifier = Modifier.fillMaxWidth()) {
            person.moments.indices.forEach { i ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(Strokes.Thin)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = if (i <= index) 1f else TRACK_ALPHA))
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            ProfileAvatar(person.username ?: "?", person.photoUrl, Sizes.AvatarS)
            Spacer(Modifier.width(Spacing.S))
            Text(
                person.username?.let { stringResource(R.string.friends_username_at, it) }.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            WhereverIconButton(onClick = onClose, modifier = Modifier.testTag("story_close")) {
                Icon(
                    Icons.Outlined.Close,
                    contentDescription = stringResource(R.string.stories_close),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun StoryFooter(moment: StoryMoment, onMoment: (String) -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().safeDrawingPadding().padding(Spacing.L),
        contentAlignment = Alignment.BottomStart
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
            moment.placeName?.let {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.S)
                ) {
                    Text(it, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    if (moment.verified) StatusPill(stringResource(R.string.stories_verified))
                }
            }
            WhereverOutlinedButton(
                text = stringResource(R.string.stories_see_post),
                onClick = { onMoment(moment.id) },
                contentColor = Color.White,
                borderColor = Color.White,
                tag = "story_see_post"
            )
        }
    }
}

private const val TRACK_ALPHA = 0.35f

private val PREVIEW_STORIES = listOf(
    StoryPerson("u1", "mariana", null, listOf(StoryMoment("m1", null, "Café Tacuba", true, "2026-11-23T10:00:00Z"))),
    StoryPerson("u2", "luis", null, listOf(StoryMoment("m2", null, null, false, "2026-11-23T11:00:00Z")))
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun StoriesRowPreview() {
    WhereverTheme { StoriesRow(PREVIEW_STORIES, onOpen = {}) }
}

@Preview
@Composable
internal fun StoryViewerPreview() {
    WhereverTheme {
        StoryViewerContent(StoryViewerUiState(loading = false, people = PREVIEW_STORIES), {}, {}, {}, {})
    }
}
