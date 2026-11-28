package com.wherever.mobile.feature.profile.ui

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
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.CircleIconButton
import com.wherever.mobile.core.designsystem.Danger
import com.wherever.mobile.core.designsystem.ErrorBanner
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.Gold
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.InlineProgress
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.Mint
import com.wherever.mobile.core.designsystem.PhaseCrossfade
import com.wherever.mobile.core.designsystem.Radius
import com.wherever.mobile.core.designsystem.RouteBlue
import com.wherever.mobile.core.designsystem.ScreenPhase
import com.wherever.mobile.core.designsystem.SegmentedControl
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.Strokes
import com.wherever.mobile.core.designsystem.Sunset
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverBottomBar
import com.wherever.mobile.core.designsystem.WhereverOutlinedButton
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.profile.domain.Badge
import com.wherever.mobile.feature.profile.domain.CityRef
import com.wherever.mobile.feature.profile.domain.NotificationSettings
import com.wherever.mobile.feature.profile.domain.Preferences
import com.wherever.mobile.feature.profile.domain.Privacy
import com.wherever.mobile.feature.profile.domain.ProfileExtras
import com.wherever.mobile.feature.profile.domain.UserProfile

/** 81_Perfil (Figma `298:8711`). */
@Composable
fun ProfileRoute(
    navigation: ProfileNavigation,
    viewModel: ProfileViewModel = hiltViewModel(),
    photoViewModel: ProfilePhotoViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val photoState by photoViewModel.state.collectAsStateWithLifecycle()
    val photoActions = rememberPhotoActions(photoViewModel)
    val snackbar = remember { SnackbarHostState() }
    val photoMessage = photoState.message?.asString()
    LaunchedEffect(photoMessage) {
        if (photoMessage != null) {
            snackbar.showSnackbar(photoMessage)
            photoViewModel.messageShown()
        }
    }
    // Contadores, insignias y reseñas por hacer cambian en otras pantallas: se releen al volver.
    LifecycleResumeEffect(viewModel) {
        viewModel.refresh()
        onPauseOrDispose { }
    }
    Scaffold(
        snackbarHost = { WhereverSnackbarHost(snackbar) },
        bottomBar = { WhereverBottomBar(MainTab.PROFILE, onSelect = navigation.onTab, onCreate = navigation.onCreate) }
    ) { padding ->
        // La barra inferior ya incluye la de navegación del sistema; sin consumirla, FormScreen la volvía a sumar.
        val bottom = PaddingValues(bottom = padding.calculateBottomPadding())
        val modifier = Modifier.padding(bottom).consumeWindowInsets(bottom)
        val phase = when {
            state.profile != null -> ScreenPhase.CONTENT
            state.loading -> ScreenPhase.LOADING
            else -> ScreenPhase.ERROR
        }
        PhaseCrossfade(phase) { shown ->
            val current = state.profile
            when {
                shown == ScreenPhase.CONTENT && current != null -> ProfileContent(
                    state = state,
                    navigation = navigation,
                    onTab = viewModel::selectTab,
                    onCoupons = navigation.onCoupons,
                    modifier = modifier,
                    avatar = { EditableProfileAvatar(current.name, current.photoUrl, photoState, photoActions) }
                )
                shown == ScreenPhase.ERROR -> ErrorState(
                    message = state.error?.asString().orEmpty(),
                    onRetry = viewModel::refresh,
                    modifier = modifier
                )
                else -> LoadingState(modifier)
            }
        }
    }
}

@Composable
internal fun ProfileContent(
    state: ProfileUiState,
    navigation: ProfileNavigation,
    onTab: (ProfileTab) -> Unit,
    onCoupons: () -> Unit,
    modifier: Modifier = Modifier,
    avatar: @Composable () -> Unit = {
        state.profile?.let { ProfileAvatar(it.name, it.photoUrl, Sizes.AvatarL) }
    }
) {
    val profile = state.profile ?: return
    FormScreen(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.S, Alignment.End)
        ) {
            CircleIconButton(
                Icons.Outlined.Notifications,
                stringResource(R.string.profile_notifications),
                navigation.onNotifications,
                Modifier.testTag("profile_notifications")
            )
            CircleIconButton(Icons.Outlined.Settings, stringResource(R.string.profile_settings), navigation.onSettings)
        }
        Header(profile, state.extras, avatar)
        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.S), verticalAlignment = Alignment.CenterVertically) {
            WhereverOutlinedButton(
                text = stringResource(R.string.profile_edit),
                onClick = navigation.onEdit,
                icon = Icons.Outlined.Edit,
                modifier = Modifier.weight(1f),
                tag = "profile_edit"
            )
            CircleIconButton(
                Icons.Outlined.Share,
                stringResource(R.string.profile_share_code),
                navigation.onMyCode,
                Modifier.testTag("profile_share")
            )
        }
        if (!profile.emailVerified) {
            InfoBanner(stringResource(R.string.profile_verify_notice), container = GoldTint, contentColor = Ink)
            WhereverTextLink(stringResource(R.string.profile_verify_action), {
                navigation.onVerifyEmail(profile.email)
            })
        }
        state.error?.let { ErrorBanner(it.asString()) }
        Counters(profile, state.extras, navigation, onPlans = { onTab(ProfileTab.PLANS) })
        BadgeRow(state.badges, navigation.onBadges)
        Shortcuts(navigation, onCoupons)
        MyReviewsRow(state.pendingReviews, navigation)
        TabsAndGrid(state, onTab, navigation)
    }
}

@Composable
private fun Header(profile: UserProfile, extras: ProfileExtras?, avatar: @Composable () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        // Anillo azul → naranja del avatar grande (docs/diseno_boceto2.md §2.2).
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .border(Strokes.Thick, Brush.linearGradient(listOf(RouteBlue, Sunset)), CircleShape)
                .padding(Strokes.Thick + Strokes.Thin)
        ) { avatar() }
        Spacer(Modifier.width(Spacing.L))
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
            Text(
                text = profile.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("profile_name")
            )
            val handle = listOfNotNull(
                profile.username?.let { stringResource(R.string.friends_username_at, it) },
                profile.city?.name
            ).joinToString(" · ")
            if (handle.isNotEmpty()) {
                Text(
                    text = handle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            extras?.description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("profile_bio")
                )
            }
        }
    }
}

@Composable
private fun Counters(profile: UserProfile, extras: ProfileExtras?, navigation: ProfileNavigation, onPlans: () -> Unit) {
    SurfaceCard(modifier = Modifier.testTag("profile_counters")) {
        Row(modifier = Modifier.fillMaxWidth().padding(vertical = Spacing.M)) {
            Counter(extras?.plans, stringResource(R.string.profile_count_plans), onPlans, Modifier.weight(1f))
            Counter(
                extras?.friends,
                stringResource(R.string.profile_count_friends),
                navigation.onFriends,
                Modifier.weight(1f)
            )
            Counter(
                extras?.reviews,
                stringResource(R.string.profile_count_reviews),
                navigation.onMyReviews,
                Modifier.weight(1f)
            )
            CounterCell(
                value = extras?.levelNumber?.let { stringResource(R.string.profile_level_number, it) } ?: profile.level,
                label = profile.level.takeIf { extras?.levelNumber != null } ?: stringResource(R.string.profile_level),
                onClick = navigation.onBadges,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun Counter(value: Int?, label: String, onClick: () -> Unit, modifier: Modifier) {
    // Sin red todavía no hay contadores: se muestra una raya, nunca un número inventado.
    CounterCell(value?.toString() ?: stringResource(R.string.profile_count_unknown), label, onClick, modifier)
}

@Composable
private fun CounterCell(value: String, label: String, onClick: () -> Unit, modifier: Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(Radius.M))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(vertical = Spacing.XS)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BadgeRow(badges: BadgeChips?, onBadges: () -> Unit) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().testTag("profile_badges")
    ) {
        val shown = badges?.shown.orEmpty()
        if (shown.isEmpty()) {
            BadgeChip(
                stringResource(R.string.profile_badges),
                painterResource(R.drawable.ic_wh_shield_check),
                RouteBlue,
                onBadges
            )
        }
        shown.forEachIndexed { index, badge ->
            val color = if (index % 2 == 0) Sunset else Mint
            BadgeChip(
                badge.name,
                painterResource(R.drawable.ic_wh_shield_check),
                color,
                onBadges,
                Modifier.weight(1f, fill = false)
            )
        }
        badges?.more?.takeIf { it > 0 }?.let { more ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(Sizes.TouchTarget)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable(role = Role.Button, onClick = onBadges)
            ) {
                Text(
                    stringResource(R.string.profile_badges_more, more),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun BadgeChip(text: String, icon: Painter, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = modifier
            .clip(CircleShape)
            .border(Strokes.Hairline, MaterialTheme.colorScheme.outlineVariant, CircleShape)
            .background(MaterialTheme.colorScheme.surface)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(start = Spacing.XS, end = Spacing.M, top = Spacing.XS, bottom = Spacing.XS)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(Sizes.IconL).clip(CircleShape).background(color)
        ) {
            Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(Sizes.IconXS))
        }
        Text(
            text,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun Shortcuts(navigation: ProfileNavigation, onCoupons: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Shortcut(
            rememberVectorPainter(Icons.Outlined.DateRange),
            RouteBlue,
            stringResource(R.string.profile_shortcut_reservations),
            navigation.onReservations,
            "profile_reservations"
        )
        Shortcut(
            rememberVectorPainter(Icons.Outlined.FavoriteBorder),
            Danger,
            stringResource(R.string.profile_shortcut_favorites),
            navigation.onFavorites,
            "profile_favorites"
        )
        Shortcut(
            painterResource(R.drawable.ic_gallery),
            Sunset,
            stringResource(R.string.profile_memories),
            navigation.onMemories,
            "profile_memories"
        )
        Shortcut(
            painterResource(R.drawable.ic_wh_ticket),
            Mint,
            stringResource(R.string.profile_shortcut_coupons),
            onCoupons,
            "profile_coupons"
        )
    }
}

@Composable
private fun Shortcut(icon: Painter, color: Color, label: String, onClick: () -> Unit, tag: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.S),
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.L))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(Spacing.XS)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(Sizes.AvatarM)
                .clip(RoundedCornerShape(Radius.L))
                .background(color.copy(alpha = SHORTCUT_TINT))
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(Sizes.IconL))
        }
        Text(label, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, maxLines = 1)
    }
}

private const val SHORTCUT_TINT = 0.16f

@Composable
private fun MyReviewsRow(pending: Int, navigation: ProfileNavigation) {
    SurfaceCard(modifier = Modifier.testTag("profile_my_reviews")) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.M),
            modifier = Modifier
                .fillMaxWidth()
                .clickable(role = Role.Button, onClick = navigation.onMyReviews)
                .padding(Spacing.M)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(Sizes.IconXXL).clip(RoundedCornerShape(Radius.M)).background(GoldTint)
            ) {
                Icon(Icons.Outlined.Star, contentDescription = null, tint = Gold, modifier = Modifier.size(Sizes.IconL))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.profile_my_reviews), style = MaterialTheme.typography.titleSmall)
                Text(
                    stringResource(R.string.profile_my_reviews_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (pending > 0) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable(role = Role.Button, onClick = navigation.onPendingReviews)
                        .testTag("profile_pending_reviews")
                ) {
                    StatusPill(
                        pluralString(R.plurals.profile_pending_new, pending),
                        container = Sunset,
                        contentColor = Color.White
                    )
                }
            }
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TabsAndGrid(state: ProfileUiState, onTab: (ProfileTab) -> Unit, navigation: ProfileNavigation) {
    SegmentedControl(
        options = listOf(
            stringResource(R.string.profile_tab_posts),
            stringResource(R.string.profile_tab_plans),
            stringResource(R.string.profile_tab_saved)
        ),
        selectedIndex = state.tab.ordinal,
        onSelect = { onTab(ProfileTab.entries[it]) },
        modifier = Modifier.testTag("profile_tabs")
    )
    val tiles = state.currentTiles
    when {
        state.tabLoading -> Box(Modifier.fillMaxWidth().padding(Spacing.XL), contentAlignment = Alignment.Center) {
            InlineProgress(Modifier.size(Sizes.IconXL))
        }
        state.tabError != null && tiles == null -> ErrorBanner(state.tabError.asString())
        tiles.isNullOrEmpty() -> Text(
            stringResource(
                when (state.tab) {
                    ProfileTab.POSTS -> R.string.profile_empty_posts
                    ProfileTab.PLANS -> R.string.profile_empty_plans
                    ProfileTab.SAVED -> R.string.profile_empty_saved
                }
            ),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(Spacing.L).testTag("profile_tab_empty")
        )
        else -> ProfileGrid(tiles) { tile ->
            when (state.tab) {
                ProfileTab.POSTS -> navigation.onPostcard(tile.id)
                ProfileTab.PLANS -> navigation.onPlan(tile.id)
                ProfileTab.SAVED -> navigation.onPlace(tile.id)
            }
        }
    }
}

@Composable
private fun pluralString(id: Int, count: Int): String = pluralStringResource(id, count, count)

private val PREVIEW_PROFILE = UserProfile(
    id = "1",
    name = "Osmar Reséndiz",
    email = "osmar@ejemplo.com",
    emailVerified = true,
    username = "osmar",
    mode = "explorar",
    city = CityRef("c1", "Tula de Allende"),
    tastes = null,
    preferences = Preferences(
        "a_pie",
        "sistema",
        "km",
        null,
        null,
        NotificationSettings(true, true, true, true, false)
    ),
    privacy = Privacy(true, true, false, true, false, false),
    points = 320,
    level = "Explorador",
    weekStreak = 2,
    deleteAt = null,
    version = 1
)

private const val PREVIEW_PLANS = 12
private const val PREVIEW_FRIENDS = 24
private const val PREVIEW_REVIEWS = 5
private const val PREVIEW_LEVEL = 3
private const val PREVIEW_PENDING = 3

@Preview
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun ProfilePreview() {
    WhereverTheme {
        ProfileContent(
            state = ProfileUiState(
                profile = PREVIEW_PROFILE,
                extras = ProfileExtras(
                    description = "Cafés, tacos y planes sin prisa.",
                    levelNumber = PREVIEW_LEVEL,
                    plans = PREVIEW_PLANS,
                    friends = PREVIEW_FRIENDS,
                    reviews = PREVIEW_REVIEWS
                ),
                loading = false,
                badges = BadgeChips(
                    listOf(
                        Badge("catador_cafe", "Catador de café", "", earned = true),
                        Badge("resenista", "Reseñista", "", earned = true)
                    ),
                    more = 4
                ),
                pendingReviews = PREVIEW_PENDING,
                tab = ProfileTab.PLANS,
                tiles = mapOf(
                    ProfileTab.PLANS to listOf(
                        ProfileTile("p1", "Domingo en Coyoacán", null, "2026-11-29"),
                        ProfileTile("p2", "Cena de viernes", null, "2026-11-27"),
                        ProfileTile("p3", "Museos del centro", null, "2026-12-05")
                    )
                )
            ),
            navigation = ProfileNavigation(),
            onTab = {},
            onCoupons = {}
        )
    }
}
