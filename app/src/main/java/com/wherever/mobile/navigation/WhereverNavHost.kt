package com.wherever.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.wherever.mobile.core.designsystem.LocalMotionEnabled
import com.wherever.mobile.core.designsystem.NavMotion
import com.wherever.mobile.core.designsystem.SystemBarIcons
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.feature.auth.ui.CheckEmailMode
import com.wherever.mobile.feature.auth.ui.CheckEmailRoute as CheckEmailScreen
import com.wherever.mobile.feature.auth.ui.LoginRoute as LoginScreen
import com.wherever.mobile.feature.auth.ui.RecoverRoute as RecoverScreen
import com.wherever.mobile.feature.auth.ui.RegisterRoute as RegisterScreen
import com.wherever.mobile.feature.auth.ui.WelcomeScreen
import com.wherever.mobile.feature.home.ui.HomeRouteScreen
import com.wherever.mobile.feature.profile.ui.EditProfileRoute as EditProfileScreen
import com.wherever.mobile.feature.profile.ui.ProfileRoute as ProfileScreen
import com.wherever.mobile.feature.settings.ui.DeleteAccountRoute as DeleteAccountScreen
import com.wherever.mobile.feature.settings.ui.DeletionPendingRoute as DeletionPendingScreen
import com.wherever.mobile.feature.settings.ui.PrivacyRoute as PrivacyScreen
import com.wherever.mobile.feature.settings.ui.SessionsRoute as SessionsScreen
import com.wherever.mobile.feature.settings.ui.SettingsNavigation
import com.wherever.mobile.feature.settings.ui.SettingsRoute as SettingsScreen
import com.wherever.mobile.feature.tastes.ui.TastesRoute as TastesScreen
import kotlinx.coroutines.flow.Flow

@Composable
fun WhereverNavHost(start: StartDestination, rootEvents: Flow<RootEvent>, pushes: Flow<PushDestination>) {
    val navController = rememberNavController()
    LaunchedEffect(navController) {
        rootEvents.collect { event ->
            when (event) {
                RootEvent.SESSION_EXPIRED -> navController.navigate(LoginRoute) { clearStack(navController) }
                RootEvent.DELETION_PENDING -> navController.navigate(DeletionPendingRoute) { clearStack(navController) }
            }
        }
    }
    LaunchedEffect(navController) {
        pushes.collect { push ->
            navController.navigate(push.route()) {
                popUpTo<HomeRoute> { inclusive = false }
                launchSingleTop = true
            }
        }
    }
    val destination = navController.currentBackStackEntryAsState().value?.destination
    SystemBarIcons(
        lightStatusIcons = destination?.hasRoute<WelcomeRoute>() == true || destination?.hasRoute<HomeRoute>() == true
    )
    val startRoute: Any = when (start) {
        StartDestination.WELCOME -> WelcomeRoute
        StartDestination.HOME -> HomeRoute
        StartDestination.DELETION_PENDING -> DeletionPendingRoute
    }
    val motion = LocalMotionEnabled.current
    NavHost(
        navController = navController,
        startDestination = startRoute,
        enterTransition = { NavMotion.forwardEnter(motion) },
        exitTransition = { NavMotion.forwardExit(motion) },
        popEnterTransition = { NavMotion.backEnter(motion) },
        popExitTransition = { NavMotion.backExit(motion) }
    ) {
        composable<WelcomeRoute> { e ->
            WelcomeScreen(
                onCreateAccount = { navController.navigateFrom(e, RegisterRoute) },
                onHaveAccount = { navController.navigateFrom(e, LoginRoute) }
            )
        }
        composable<LoginRoute> { e ->
            LoginScreen(
                onBack = {
                    // Tras cerrar sesión, Login es la raíz de la pila.
                    if (e.isActive() && !navController.popBackStack()) navController.navigate(WelcomeRoute)
                },
                onForgotPassword = { navController.navigateFrom(e, RecoverRoute) },
                onCreateAccount = { navController.navigateFrom(e, RegisterRoute) },
                onLoggedIn = { destination ->
                    val route: Any = when (destination) {
                        PostAuthDestination.HOME -> HomeRoute
                        PostAuthDestination.TASTES -> TastesRoute()
                        PostAuthDestination.DELETION_PENDING -> DeletionPendingRoute
                    }
                    navController.navigateOnResult(e, route) { clearStack(navController) }
                }
            )
        }
        composable<RegisterRoute> { e ->
            RegisterScreen(
                onBack = { navController.popFrom(e) },
                onRegistered = { navController.navigateOnResult(e, TastesRoute()) { clearStack(navController) } }
            )
        }
        composable<RecoverRoute> { e ->
            RecoverScreen(
                onBack = { navController.popFrom(e) },
                onSent = { email ->
                    navController.navigateOnResult(e, CheckEmailRoute(CheckEmailMode.RECOVERY.name, email))
                }
            )
        }
        composable<CheckEmailRoute>(
            enterTransition = { NavMotion.resultEnter(motion) },
            exitTransition = { NavMotion.resultExit(motion) },
            popEnterTransition = { NavMotion.resultEnter(motion) },
            popExitTransition = { NavMotion.resultExit(motion) }
        ) { e -> CheckEmailScreen(onBack = { navController.popFrom(e) }) }
        composable<TastesRoute> { e ->
            TastesScreen(
                onBack = { navController.popFrom(e) },
                onFinished = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popOnResult(e)
                    } else {
                        navController.navigateOnResult(e, HomeRoute) { clearStack(navController) }
                    }
                }
            )
        }
        composable<HomeRoute> { e ->
            RequestNotificationPermission()
            HomeRouteScreen(onProfile = { navController.navigateFrom(e, ProfileRoute) })
        }
        composable<ProfileRoute> { e ->
            ProfileScreen(
                onHome = { if (e.isActive()) navController.popBackStack(HomeRoute, inclusive = false) },
                onEdit = { navController.navigateFrom(e, EditProfileRoute) },
                onSettings = { navController.navigateFrom(e, SettingsRoute) },
                onVerifyEmail = { email ->
                    navController.navigateFrom(e, CheckEmailRoute(CheckEmailMode.VERIFICATION.name, email))
                }
            )
        }
        composable<EditProfileRoute> { e ->
            EditProfileScreen(
                onBack = { navController.popFrom(e) },
                onTastes = { navController.navigateFrom(e, TastesRoute(fromSettings = true)) },
                onPrivacy = { navController.navigateFrom(e, PrivacyRoute) },
                onDeleteAccount = { navController.navigateFrom(e, DeleteAccountRoute) }
            )
        }
        composable<SettingsRoute> { e ->
            SettingsScreen(
                SettingsNavigation(
                    onBack = { navController.popFrom(e) },
                    onPersonalData = { navController.navigateFrom(e, EditProfileRoute) },
                    onTastes = { navController.navigateFrom(e, TastesRoute(fromSettings = true)) },
                    onSessions = { navController.navigateFrom(e, SessionsRoute) },
                    onPrivacy = { navController.navigateFrom(e, PrivacyRoute) },
                    onLoggedOut = { navController.navigateOnResult(e, LoginRoute) { clearStack(navController) } }
                )
            )
        }
        composable<SessionsRoute> { e ->
            SessionsScreen(
                onBack = { navController.popFrom(e) },
                onAllClosed = { navController.navigateOnResult(e, LoginRoute) { clearStack(navController) } }
            )
        }
        composable<PrivacyRoute> { e -> PrivacyScreen(onBack = { navController.popFrom(e) }) }
        composable<DeleteAccountRoute> { e ->
            DeleteAccountScreen(
                onBack = { navController.popFrom(e) },
                onDeleted = { navController.navigateOnResult(e, LoginRoute) { clearStack(navController) } }
            )
        }
        composable<DeletionPendingRoute>(
            enterTransition = { NavMotion.resultEnter(motion) },
            exitTransition = { NavMotion.resultExit(motion) },
            popEnterTransition = { NavMotion.resultEnter(motion) },
            popExitTransition = { NavMotion.resultExit(motion) }
        ) { e ->
            DeletionPendingScreen(
                onCancelled = { navController.navigateOnResult(e, HomeRoute) { clearStack(navController) } },
                onLoggedOut = { navController.navigateOnResult(e, LoginRoute) { clearStack(navController) } }
            )
        }
    }
}

/** Al entrar o salir de sesión no debe quedar nada atrás. */
private fun NavOptionsBuilder.clearStack(navController: NavHostController) {
    popUpTo(navController.graph.id) { inclusive = true }
    launchSingleTop = true
}
