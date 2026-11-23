package com.wherever.mobile.feature.business.ui

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.LoadingState
import com.wherever.mobile.core.designsystem.Sizes
import kotlinx.serialization.Serializable

/** Rutas internas del modo negocio (docs/routes.md). El grafo principal solo conoce `BusinessHomeRoute`. */
@Serializable
internal data object BusinessPanelDestination

@Serializable
internal data object BusinessReservationsDestination

@Serializable
internal data object BusinessStatsDestination

@Serializable
internal data class BusinessCheckinDestination(val code: String? = null)

@Serializable
internal data object BusinessCouponsDestination

@Serializable
internal data object BusinessCouponFormDestination

@Serializable
internal data object BusinessCapacityDestination

@Serializable
internal data object BusinessReviewsDestination

/** 52_NegResp recibe la reseña elegida en 51 (no hay `GET` de una sola reseña). */
@Serializable
internal data class BusinessReplyDestination(
    val reviewId: String,
    val rating: Int,
    val comment: String,
    val author: String,
    val createdAt: String,
    val currentReply: String? = null,
    val progress: Int? = null
) {
    fun target() = ReplyTarget(reviewId, rating, comment, author, createdAt, currentReply, progress)
}

@Serializable
internal data object BusinessMomentsDestination

/** Aviso que 52_NegResp deja a 51_NegResenas al publicar (id de `R.string`). */
private const val REPLY_MESSAGE_KEY = "biz_reply_message"

/**
 * Pestañas de la barra del negocio que ya tienen pantalla.
 * Perfil (nv-38) llega con su spec.
 */
enum class BusinessTab { PANEL, RESERVATIONS, COUPONS, REVIEWS }

/**
 * Modo negocio (specs/nv-35): entra con `POST /usuarios/yo/modo` y abre el Panel del negocio activo.
 * Sin rol comerciante o sin negocio enviado, [onOnboarding] lleva al alta (nv-34).
 */
@Composable
fun BusinessHomeRoute(onOnboarding: () -> Unit, viewModel: BusinessHomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.needsOnboarding.collect { onOnboarding() } }
    val error = state.error
    val selected = state.selected
    when {
        error != null -> ErrorState(error.asString(), viewModel::load)
        selected == null -> LoadingState()
        else -> BusinessShell(BusinessHeader(state.businesses, selected.id, viewModel::select))
    }
}

@Composable
private fun BusinessShell(header: BusinessHeader) {
    val navController = rememberNavController()
    val entry by navController.currentBackStackEntryAsState()
    val destination = entry?.destination
    Scaffold(
        // Cada pantalla pone sus márgenes: la portada del Panel va debajo de la barra de estado, como en Figma.
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            // 50_NegCheckin va sin barra, como en la maqueta.
            if (destination?.hasRoute<BusinessCheckinDestination>() != true) {
                BusinessBar(destination.tab()) { navController.openTab(it) }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = BusinessPanelDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable<BusinessPanelDestination> {
                BusinessPanelRoute(
                    header = header,
                    onStats = { navController.navigate(BusinessStatsDestination) { launchSingleTop = true } },
                    onReservations = { navController.openTab(BusinessTab.RESERVATIONS) },
                    onCapacity = { navController.navigate(BusinessCapacityDestination) { launchSingleTop = true } }
                )
            }
            composable<BusinessCouponsDestination> {
                CouponsRoute(
                    businessId = header.selectedId,
                    onCreate = { navController.navigate(BusinessCouponFormDestination) { launchSingleTop = true } }
                )
            }
            composable<BusinessCouponFormDestination> {
                CouponFormRoute(header.selectedId, onBack = { navController.popBackStack() })
            }
            composable<BusinessCapacityDestination> {
                CapacityRoute(header.selectedId, onBack = { navController.popBackStack() })
            }
            composable<BusinessReviewsDestination> { backStack ->
                val message by backStack.savedStateHandle.getStateFlow<Int?>(REPLY_MESSAGE_KEY, null)
                    .collectAsStateWithLifecycle()
                BusinessReviewsRoute(
                    businessId = header.selectedId,
                    message = message,
                    onMessageShown = { backStack.savedStateHandle[REPLY_MESSAGE_KEY] = null },
                    onReply = { review, progress ->
                        navController.navigate(
                            BusinessReplyDestination(
                                review.id,
                                review.rating,
                                review.comment,
                                review.author,
                                review.createdAt,
                                review.businessReply,
                                progress
                            )
                        ) { launchSingleTop = true }
                    },
                    onMoments = { navController.navigate(BusinessMomentsDestination) { launchSingleTop = true } }
                )
            }
            composable<BusinessReplyDestination> { backStack ->
                ReviewReplyRoute(
                    businessId = header.selectedId,
                    target = backStack.toRoute<BusinessReplyDestination>().target(),
                    onBack = { navController.popBackStack() },
                    onPublished = { message ->
                        navController.previousBackStackEntry?.savedStateHandle?.set(REPLY_MESSAGE_KEY, message)
                        navController.popBackStack()
                    }
                )
            }
            composable<BusinessMomentsDestination> {
                BusinessMomentsRoute(header.selectedId, onBack = { navController.popBackStack() })
            }
            composable<BusinessStatsDestination> {
                BusinessStatsRoute(header.selectedId, onBack = { navController.popBackStack() })
            }
            composable<BusinessReservationsDestination> {
                BusinessReservationsRoute(
                    businessId = header.selectedId,
                    onCheckin = { code ->
                        navController.navigate(BusinessCheckinDestination(code)) { launchSingleTop = true }
                    }
                )
            }
            composable<BusinessCheckinDestination> { backStack ->
                CheckinRoute(
                    businessId = header.selectedId,
                    code = backStack.toRoute<BusinessCheckinDestination>().code,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private fun NavDestination?.tab(): BusinessTab = when {
    this == null -> BusinessTab.PANEL
    hasRoute<BusinessReservationsDestination>() || hasRoute<BusinessCheckinDestination>() -> BusinessTab.RESERVATIONS
    hasRoute<BusinessCouponsDestination>() || hasRoute<BusinessCouponFormDestination>() -> BusinessTab.COUPONS
    hasRoute<BusinessReviewsDestination>() ||
        hasRoute<BusinessReplyDestination>() ||
        hasRoute<BusinessMomentsDestination>() -> BusinessTab.REVIEWS
    else -> BusinessTab.PANEL
}

private fun NavHostController.openTab(tab: BusinessTab) {
    val route: Any = when (tab) {
        BusinessTab.PANEL -> BusinessPanelDestination
        BusinessTab.RESERVATIONS -> BusinessReservationsDestination
        BusinessTab.COUPONS -> BusinessCouponsDestination
        BusinessTab.REVIEWS -> BusinessReviewsDestination
    }
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Barra del negocio de 48_NegPanel, con los componentes de Material del tema (mismo estilo que la barra principal). */
@Composable
internal fun BusinessBar(selected: BusinessTab, onSelect: (BusinessTab) -> Unit) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.navigationBarsPadding().testTag("business_bar")
    ) {
        BusinessTab.entries.forEach { tab ->
            val (icon, label) = when (tab) {
                BusinessTab.PANEL -> painterResource(R.drawable.ic_wh_bar_chart) to R.string.business_tab_panel
                BusinessTab.RESERVATIONS -> rememberVectorPainter(Icons.Outlined.DateRange) to
                    R.string.business_tab_reservations
                BusinessTab.COUPONS -> painterResource(R.drawable.ic_wh_ticket) to R.string.business_tab_coupons
                BusinessTab.REVIEWS -> rememberVectorPainter(Icons.Outlined.Star) to
                    R.string.business_tab_reviews
            }
            BarItem(tab == selected, icon, label, "business_bar_${tab.name.lowercase()}") { onSelect(tab) }
        }
    }
}

@Composable
private fun RowScope.BarItem(
    selected: Boolean,
    icon: Painter,
    @StringRes label: Int,
    tag: String,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.IconL)) },
        label = { Text(stringResource(label)) },
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.primary,
            selectedTextColor = MaterialTheme.colorScheme.primary,
            indicatorColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.testTag(tag)
    )
}
