package com.wherever.mobile.feature.home.ui

import android.Manifest
import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.CreateOption
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.UnderlineTabs
import com.wherever.mobile.core.designsystem.WhereverBottomBar
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.core.notifications.RequestNotificationPermission
import com.wherever.mobile.core.ui.formatDateTime
import com.wherever.mobile.feature.feed.domain.StoryPerson
import com.wherever.mobile.feature.feed.ui.FriendsFeedActions
import com.wherever.mobile.feature.feed.ui.FriendsFeedUiState
import com.wherever.mobile.feature.feed.ui.FriendsFeedViewModel
import com.wherever.mobile.feature.feed.ui.PREVIEW_FEED_ITEM
import com.wherever.mobile.feature.feed.ui.StoriesRow
import com.wherever.mobile.feature.feed.ui.StoriesViewModel
import com.wherever.mobile.feature.feed.ui.friendsFeedItems
import com.wherever.mobile.feature.home.domain.Company
import com.wherever.mobile.feature.home.domain.Event
import com.wherever.mobile.feature.notifications.ui.UnreadBadgeViewModel
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.plans.domain.PopularPlan
import java.time.ZoneId

private const val CENTS = 100
private const val SKELETON_CARDS = 3

data class HomeNavigation(
    val onTab: (MainTab) -> Unit,
    val onCreate: (CreateOption) -> Unit,
    val onSearch: () -> Unit,
    val onCategory: (String) -> Unit,
    val onPlace: (String) -> Unit,
    val onPlan: (String) -> Unit,
    val onNotifications: () -> Unit,
    val onMoment: (String) -> Unit,
    val onFriendPlan: (String) -> Unit,
    val onPerson: (String) -> Unit,
    val onAddFriends: () -> Unit,
    /** Visor de historias desde la persona tocada (backend nv-84). */
    val onStory: (String) -> Unit = {}
)

/** 10_Inicio. */
@Composable
fun HomeRouteScreen(
    navigation: HomeNavigation,
    viewModel: HomeViewModel = hiltViewModel(),
    badge: UnreadBadgeViewModel = hiltViewModel(),
    feed: FriendsFeedViewModel = hiltViewModel(),
    stories: StoriesViewModel = hiltViewModel()
) {
    val storiesState by stories.state.collectAsStateWithLifecycle()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val feedState by feed.state.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableIntStateOf(HomeTab.NEARBY.ordinal) }
    // "Planes de tus amigos" sale del mismo feed, así que se carga desde que se abre Inicio.
    LaunchedEffect(feed) { feed.onShown() }
    val unread by badge.count.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    val layoutDirection = LocalLayoutDirection.current
    val undoMessage = stringResource(R.string.home_not_interested_done)
    val undoLabel = stringResource(R.string.home_undo)
    LaunchedEffect(viewModel) { viewModel.openPlan.collect(navigation.onPlan) }
    if (state.askNotifications) RequestNotificationPermission()
    LifecycleResumeEffect(viewModel) {
        viewModel.onResume()
        badge.refresh()
        onPauseOrDispose { }
    }
    LaunchedEffect(state.dismissed) {
        if (state.dismissed != null) {
            val result = snackbar.showSnackbar(undoMessage, undoLabel, duration = SnackbarDuration.Short)
            if (result == SnackbarResult.ActionPerformed) viewModel.undoNotInterested() else viewModel.undoExpired()
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        viewModel.onPermissionResult(it)
    }
    Scaffold(
        snackbarHost = { WhereverSnackbarHost(snackbar) },
        bottomBar = { WhereverBottomBar(MainTab.HOME, onSelect = navigation.onTab, onCreate = navigation.onCreate) }
    ) { padding ->
        HomeContent(
            state = state,
            unread = unread,
            navigation = navigation,
            friends = HomeFriendsTab(
                selected = tab == HomeTab.FRIENDS.ordinal,
                state = feedState,
                stories = storiesState.people,
                onSelectTab = { tab = it },
                actions = FriendsFeedActions(
                    onMoment = navigation.onMoment,
                    onPlace = navigation.onPlace,
                    onPlan = navigation.onFriendPlan,
                    onPerson = navigation.onPerson,
                    onAddFriends = navigation.onAddFriends,
                    onRequest = feed::request,
                    onRetry = feed::refresh,
                    onLoadMore = feed::loadMore
                )
            ),
            actions = HomeActions(
                onRefresh = viewModel::refresh,
                onCompany = viewModel::onCompany,
                onNotInterested = viewModel::notInterested,
                onAskLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                onLocationNotNow = viewModel::onLocationNotNow,
                onCopyPlan = viewModel::copyPopular
            ),
            // Sin el margen de arriba (la portada va bajo la barra de estado), pero sí los laterales: en
            // horizontal la barra de navegación del sistema tapaba la campana y la chispa de IA.
            modifier = Modifier.padding(
                start = padding.calculateStartPadding(layoutDirection),
                end = padding.calculateEndPadding(layoutDirection),
                bottom = padding.calculateBottomPadding()
            )
        )
    }
}

/** Pestañas de 10_Inicio y 11_InicioAmigos. */
enum class HomeTab { NEARBY, FRIENDS }

/** Estado y acciones de la pestaña Amigos (specs/nv-17). */
data class HomeFriendsTab(
    val selected: Boolean,
    val state: FriendsFeedUiState,
    val onSelectTab: (Int) -> Unit,
    val actions: FriendsFeedActions,
    val stories: List<StoryPerson> = emptyList()
)

data class HomeActions(
    val onRefresh: () -> Unit,
    val onCompany: (Company) -> Unit,
    val onNotInterested: (PlaceCard) -> Unit,
    val onAskLocation: () -> Unit,
    val onLocationNotNow: () -> Unit,
    val onCopyPlan: (PopularPlan) -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    state: HomeUiState,
    unread: Int,
    navigation: HomeNavigation,
    friends: HomeFriendsTab,
    actions: HomeActions,
    modifier: Modifier
) {
    PullToRefreshBox(
        isRefreshing = if (friends.selected) false else state.refreshing,
        onRefresh = if (friends.selected) friends.actions.onRetry else actions.onRefresh,
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("home_refresh")
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = Spacing.XXL),
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.fillMaxSize().testTag("home_list")
        ) {
            item {
                HomeCover(
                    state = state,
                    unread = unread,
                    onSearch = navigation.onSearch,
                    onAiPlan = { navigation.onCreate(CreateOption.AI_PLAN) },
                    onNotifications = navigation.onNotifications
                )
            }
            item {
                UnderlineTabs(
                    options = listOf(
                        stringResource(R.string.home_tab_nearby),
                        stringResource(R.string.home_tab_friends)
                    ),
                    selectedIndex = if (friends.selected) HomeTab.FRIENDS.ordinal else HomeTab.NEARBY.ordinal,
                    onSelect = friends.onSelectTab,
                    modifier = Modifier.padding(horizontal = ScreenPadding),
                    tag = "home_tabs"
                )
            }
            if (friends.selected) {
                if (friends.stories.isNotEmpty()) {
                    item(key = "stories") { StoriesRow(friends.stories, onOpen = navigation.onStory) }
                }
                friendsFeedItems(friends.state, friends.actions)
            } else {
                nearbyItems(state, navigation, actions, friends)
            }
        }
    }
}

/** Contenido de "Cerca de ti" (10_Inicio): carrusel de lugares, planes de amigos y lo demás de la spec debajo. */
private fun LazyListScope.nearbyItems(
    state: HomeUiState,
    navigation: HomeNavigation,
    actions: HomeActions,
    friends: HomeFriendsTab
) {
    if (state.locationPrompt != LocationPrompt.NONE) item { LocationCard(state.locationPrompt, actions) }
    state.error?.let { error ->
        item { ErrorBanner(error.asString(), Modifier.padding(horizontal = ScreenPadding)) }
    }
    // Con error y sin lugares solo queda el aviso: el título con "Ver todo" no tendría nada debajo.
    if (state.error == null || state.recommendations.isNotEmpty()) {
        item {
            SectionHeader(
                title = stringResource(R.string.home_tab_nearby),
                action = stringResource(R.string.home_see_all),
                onAction = { navigation.onTab(MainTab.EXPLORE) },
                tag = "home_see_all"
            )
        }
        when {
            state.loading -> item { NearbySkeleton() }
            state.recommendations.isEmpty() -> item { EmptyRecommendations(navigation.onSearch) }
            else -> item { NearbyCarousel(state.recommendations, navigation.onPlace, actions.onNotInterested) }
        }
    }
    val friendPlans = friends.state.items.filter { it.moment.planId != null }
    if (friendPlans.isNotEmpty()) {
        item {
            SectionHeader(
                title = stringResource(R.string.home_friend_plans),
                action = stringResource(R.string.feed_see_more),
                onAction = { friends.onSelectTab(HomeTab.FRIENDS.ordinal) },
                tag = "home_friend_plans_more"
            )
        }
        item { FriendPlansCarousel(friendPlans, navigation.onFriendPlan) }
    }
    popularAndEvents(state, actions)
    // Fuera del Boceto 2 pero en la spec (US-062, US-025): al final, no en la portada (docs/revision-jorge.md).
    item { CompanyChips(state.company, actions.onCompany) }
    if (state.chips.isNotEmpty()) item { MoodChips(state, navigation.onCategory) }
}

@Composable
private fun NearbySkeleton() {
    LazyRow(
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.M),
        userScrollEnabled = false,
        modifier = Modifier.testTag("home_nearby_loading")
    ) {
        items(SKELETON_CARDS) {
            Box(
                Modifier
                    .size(width = Sizes.NearbyCardWidth, height = Sizes.NearbyCardHeight)
                    .clip(WhereverShapes.Card)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

/** Planes populares y eventos al final de "Cerca de ti". */
private fun LazyListScope.popularAndEvents(state: HomeUiState, actions: HomeActions) {
    if (state.popular.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.home_popular),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = ScreenPadding)
            )
        }
        items(state.popular, key = { "popular_${it.id}" }) { plan ->
            PopularPlanCard(plan) { actions.onCopyPlan(plan) }
        }
    }
    if (state.events.isNotEmpty()) {
        item {
            Text(
                stringResource(R.string.home_events),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = ScreenPadding)
            )
        }
        item {
            LazyRow(
                contentPadding = PaddingValues(horizontal = ScreenPadding),
                horizontalArrangement = Arrangement.spacedBy(Spacing.M)
            ) {
                items(state.events, key = { it.id }) { EventCard(it) }
            }
        }
    }
}

private val COMPANIES = listOf(
    Company.SOLO to R.string.company_solo,
    Company.COUPLE to R.string.company_couple,
    Company.FRIENDS to R.string.company_friends,
    Company.FAMILY to R.string.company_family
)

/** US-062: con quién sales. Tocar la activa la quita. */
@Composable
private fun CompanyChips(selected: Company?, onCompany: (Company) -> Unit) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = ScreenPadding),
        horizontalArrangement = Arrangement.spacedBy(Spacing.S)
    ) {
        items(COMPANIES) { (company, label) ->
            SelectChip(
                stringResource(label),
                selected == company,
                { onCompany(company) },
                tag = "company_${company.apiValue}"
            )
        }
    }
}

@Composable
private fun MoodChips(state: HomeUiState, onCategory: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.S)) {
        Text(
            stringResource(R.string.home_cravings),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(horizontal = ScreenPadding)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = ScreenPadding),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S)
        ) {
            items(state.chips, key = { it.id }) { chip ->
                SelectChip(chip.name, selected = false, onClick = {
                    onCategory(chip.category)
                }, tag = "mood_${chip.id}")
            }
        }
    }
}

@Composable
private fun LocationCard(prompt: LocationPrompt, actions: HomeActions) {
    val context = LocalContext.current
    SurfaceCard(modifier = Modifier.padding(horizontal = ScreenPadding).testTag("home_location_card")) {
        Column(modifier = Modifier.padding(Spacing.L), verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Text(stringResource(R.string.location_title), style = MaterialTheme.typography.titleMedium)
            Text(
                stringResource(
                    if (prompt == LocationPrompt.ASK) R.string.location_body else R.string.location_settings_body
                ),
                style = MaterialTheme.typography.bodyMedium
            )
            if (prompt == LocationPrompt.ASK) {
                WhereverPrimaryButton(
                    stringResource(R.string.location_allow),
                    actions.onAskLocation,
                    tag = "location_allow"
                )
            } else {
                WhereverPrimaryButton(
                    stringResource(R.string.location_open_settings),
                    onClick = {
                        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            .setData("package:${context.packageName}".toUri())
                        context.startActivity(intent)
                    },
                    tag = "location_settings"
                )
            }
            WhereverTextButton(onClick = actions.onLocationNotNow, modifier = Modifier.testTag("location_not_now")) {
                Text(stringResource(R.string.location_not_now))
            }
        }
    }
}

@Composable
private fun EmptyRecommendations(onSearch: () -> Unit) {
    Column(
        modifier = Modifier.padding(horizontal = ScreenPadding),
        verticalArrangement = Arrangement.spacedBy(Spacing.S)
    ) {
        InfoBanner(stringResource(R.string.home_empty), tag = "home_empty")
        WhereverTextLink(stringResource(R.string.home_empty_action), onSearch)
    }
}

/** US-033: plan popular de la semana; "Copiar" lo guarda para mañana. */
@Composable
private fun PopularPlanCard(plan: PopularPlan, onCopy: () -> Unit) {
    SurfaceCard(modifier = Modifier.padding(horizontal = ScreenPadding).testTag("popular_${plan.id}")) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.L)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(plan.name, style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.home_popular_detail, plan.stops.size, plan.costPerPerson, plan.copies),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            WhereverTextButton(onClick = onCopy) { Text(stringResource(R.string.home_popular_copy)) }
        }
    }
}

/** US-030: eventos, ferias y tianguis de los próximos 7 días. */
@Composable
private fun EventCard(event: Event) {
    SurfaceCard(modifier = Modifier.width(Sizes.EventCardWidth).testTag("event_${event.id}")) {
        Column(modifier = Modifier.padding(Spacing.M), verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Text(event.title, style = MaterialTheme.typography.titleSmall, maxLines = 2)
            Text(
                formatDateTime(event.startsAt, ZoneId.systemDefault()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            if (event.address.isNotBlank()) {
                Text(
                    event.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
            Text(
                if (event.priceCents == 0) {
                    stringResource(R.string.home_event_free)
                } else {
                    stringResource(R.string.place_price, event.priceCents / CENTS)
                },
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private const val PREVIEW_UNREAD = 3

private val PREVIEW_PLACES = listOf(
    PlaceCard(
        id = "p1",
        name = "Café Raíz",
        category = "cafeteria",
        priceLevel = 2,
        averagePrice = 120,
        rating = 4.8,
        reviews = 120,
        coverUrl = null,
        distanceMeters = 640.0,
        openNow = true,
        why = "Te gustan los cafés"
    ),
    PlaceCard(
        id = "p2",
        name = "Jardín del Cerro",
        category = "entretenimiento",
        priceLevel = 1,
        averagePrice = 80,
        rating = 4.6,
        reviews = 54,
        coverUrl = null,
        distanceMeters = 1120.0,
        openNow = true
    ),
    PlaceCard(
        id = "p3",
        name = "Fonda Doña Lupe",
        category = "restaurante",
        priceLevel = 2,
        averagePrice = 150,
        rating = 4.7,
        reviews = 88,
        coverUrl = null,
        distanceMeters = null,
        openNow = false
    )
)

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun HomePreview() {
    WhereverTheme {
        HomeContent(
            state = HomeUiState(
                loading = false,
                firstName = "Osmar",
                city = "Tula de Allende",
                recommendations = PREVIEW_PLACES
            ),
            unread = PREVIEW_UNREAD,
            navigation = HomeNavigation({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}),
            friends = HomeFriendsTab(
                selected = false,
                state = FriendsFeedUiState(loading = false, items = listOf(PREVIEW_FEED_ITEM)),
                onSelectTab = {},
                actions = FriendsFeedActions({}, {}, {}, {}, {}, {}, {}, {})
            ),
            actions = HomeActions({}, {}, {}, {}, {}, {}),
            modifier = Modifier
        )
    }
}
