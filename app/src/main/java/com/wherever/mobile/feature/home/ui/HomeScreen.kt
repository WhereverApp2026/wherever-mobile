package com.wherever.mobile.feature.home.ui

import android.Manifest
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPadding
import com.wherever.mobile.core.designsystem.SelectChip
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverBottomBar
import com.wherever.mobile.core.designsystem.WhereverIconButton
import com.wherever.mobile.core.designsystem.WhereverPrimaryButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextButton
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.notifications.RequestNotificationPermission
import com.wherever.mobile.core.ui.formatDateTime
import com.wherever.mobile.feature.home.domain.Company
import com.wherever.mobile.feature.home.domain.Event
import com.wherever.mobile.feature.notifications.ui.UnreadBadgeViewModel
import com.wherever.mobile.feature.places.domain.PlaceCard
import com.wherever.mobile.feature.places.ui.PlaceCardItem
import com.wherever.mobile.feature.places.ui.PlaceCardSkeleton
import com.wherever.mobile.feature.plans.domain.PopularPlan
import java.time.ZoneId

private const val CENTS = 100
private const val SKELETON_CARDS = 3

data class HomeNavigation(
    val onTab: (MainTab) -> Unit,
    val onSearch: () -> Unit,
    val onCategory: (String) -> Unit,
    val onPlace: (String) -> Unit,
    val onPlan: (String) -> Unit,
    val onNotifications: () -> Unit
)

/** 10_Inicio. */
@Composable
fun HomeRouteScreen(
    navigation: HomeNavigation,
    viewModel: HomeViewModel = hiltViewModel(),
    badge: UnreadBadgeViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val unread by badge.count.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
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
        bottomBar = { WhereverBottomBar(MainTab.HOME, onSelect = navigation.onTab) }
    ) { padding ->
        HomeContent(
            state = state,
            unread = unread,
            navigation = navigation,
            actions = HomeActions(
                onRefresh = viewModel::refresh,
                onCompany = viewModel::onCompany,
                onNotInterested = viewModel::notInterested,
                onAskLocation = { permissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION) },
                onLocationNotNow = viewModel::onLocationNotNow,
                onCopyPlan = viewModel::copyPopular
            ),
            modifier = Modifier.padding(bottom = padding.calculateBottomPadding())
        )
    }
}

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
    actions: HomeActions,
    modifier: Modifier
) {
    PullToRefreshBox(
        isRefreshing = state.refreshing,
        onRefresh = actions.onRefresh,
        modifier = modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).testTag("home_refresh")
    ) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = Spacing.XXL),
            verticalArrangement = Arrangement.spacedBy(Spacing.L),
            modifier = Modifier.fillMaxSize().testTag("home_list")
        ) {
            item { Header(state, unread, navigation) }
            item { CompanyChips(state.company, actions.onCompany) }
            if (state.chips.isNotEmpty()) item { MoodChips(state, navigation.onCategory) }
            if (state.locationPrompt != LocationPrompt.NONE) item { LocationCard(state.locationPrompt, actions) }
            state.error?.let { error ->
                item { ErrorBanner(error.asString(), Modifier.padding(horizontal = ScreenPadding)) }
            }
            item {
                Text(
                    stringResource(R.string.home_for_you),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = ScreenPadding)
                )
            }
            when {
                state.loading -> items(SKELETON_CARDS) {
                    PlaceCardSkeleton(Modifier.padding(horizontal = ScreenPadding))
                }
                state.recommendations.isEmpty() && state.error == null -> item {
                    EmptyRecommendations(navigation.onSearch)
                }
                else -> items(state.recommendations, key = { it.id }) { place ->
                    PlaceCardItem(
                        place = place,
                        onClick = { navigation.onPlace(place.id) },
                        modifier = Modifier.padding(horizontal = ScreenPadding).animateItem()
                    ) {
                        place.why?.let { why ->
                            Text(
                                stringResource(R.string.home_why, why),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.testTag("home_why_${place.id}")
                            )
                        }
                        WhereverTextLink(
                            stringResource(R.string.home_not_interested),
                            onClick = { actions.onNotInterested(place) }
                        )
                    }
                }
            }
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
    }
}

@Composable
private fun Header(state: HomeUiState, unread: Int, navigation: HomeNavigation) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(bottomStart = Radius.XXXL, bottomEnd = Radius.XXXL))
            .background(Brush.verticalGradient(listOf(Ink, RouteBlue)))
            .statusBarsPadding()
            .padding(ScreenPadding)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.M)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (state.city != null) {
                    Icon(Icons.Outlined.LocationOn, contentDescription = null, tint = Color.White)
                    Text(text = state.city, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(Modifier.weight(1f))
                state.weather?.let { weather ->
                    Surface(shape = CircleShape, color = Color.White.copy(alpha = 0.18f), contentColor = Color.White) {
                        Text(
                            stringResource(R.string.home_weather, weather.temperatureC, weather.condition),
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(
                                horizontal = Spacing.M,
                                vertical = Spacing.S
                            ).testTag("home_weather")
                        )
                    }
                }
                NotificationBell(unread, navigation.onNotifications)
            }
            Text(
                text = state.firstName?.let { stringResource(R.string.home_greeting_name, it) }
                    ?: stringResource(R.string.home_greeting),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                modifier = Modifier.testTag("home_greeting")
            )
            if (state.greeting.isNotBlank()) {
                Text(
                    state.greeting,
                    color = Color.White.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
            if (state.weather?.adverse == true) {
                Text(
                    stringResource(R.string.home_weather_adverse),
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            SearchBox(navigation.onSearch)
        }
    }
}

/** La caja "¿Qué se te antoja?" abre Explorar con el teclado listo. */
@Composable
private fun SearchBox(onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(Radius.L),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.RowMinHeight)
            .clip(RoundedCornerShape(Radius.L))
            .clickable(onClick = onClick)
            .semantics { role = Role.Button }
            .testTag("home_search")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(Spacing.L)) {
            Icon(Icons.Outlined.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(Spacing.M))
            Text(stringResource(R.string.home_search_hint), color = MaterialTheme.colorScheme.onSurfaceVariant)
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

/** Campana de avisos con el número de no leídos (specs/nv-52). */
@Composable
private fun NotificationBell(unread: Int, onClick: () -> Unit) {
    val description = if (unread > 0) {
        pluralStringResource(R.plurals.home_notifications_unread, unread, unread)
    } else {
        stringResource(R.string.notifications_title)
    }
    WhereverIconButton(onClick = onClick, modifier = Modifier.testTag("home_notifications")) {
        BadgedBox(
            badge = {
                if (unread > 0) {
                    Badge(containerColor = Sunset, contentColor = Color.White) {
                        Text(if (unread > MAX_BADGE) "$MAX_BADGE+" else unread.toString())
                    }
                }
            }
        ) {
            Icon(Icons.Outlined.Notifications, contentDescription = description, tint = Color.White)
        }
    }
}

private const val MAX_BADGE = 99
