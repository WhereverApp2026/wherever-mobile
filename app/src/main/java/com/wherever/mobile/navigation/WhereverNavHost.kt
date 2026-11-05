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
import com.wherever.mobile.core.designsystem.SystemBarIcons
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
fun WhereverNavHost(start: StartDestination, rootEvents: Flow<RootEvent>) {
    val navController = rememberNavController()
    LaunchedEffect(navController) {
        rootEvents.collect { event ->
            when (event) {
                RootEvent.SESSION_EXPIRED -> navController.navigate(LoginRoute) { clearStack(navController) }
                RootEvent.DELETION_PENDING -> navController.navigate(DeletionPendingRoute) { clearStack(navController) }
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
    NavHost(navController = navController, startDestination = startRoute) {
        composable<WelcomeRoute> {
            WelcomeScreen(
                onCreateAccount = { navController.navigate(RegisterRoute) },
                onHaveAccount = { navController.navigate(LoginRoute) }
            )
        }
        composable<LoginRoute> {
            LoginScreen(
                onBack = {
                    // Tras cerrar sesión, Login es la raíz de la pila.
                    if (!navController.popBackStack()) navController.navigate(WelcomeRoute)
                },
                onForgotPassword = { navController.navigate(RecoverRoute) },
                onCreateAccount = { navController.navigate(RegisterRoute) },
                onLoggedIn = { destination ->
                    val route: Any = when (destination) {
                        PostAuthDestination.HOME -> HomeRoute
                        PostAuthDestination.TASTES -> TastesRoute()
                        PostAuthDestination.DELETION_PENDING -> DeletionPendingRoute
                    }
                    navController.navigate(route) { clearStack(navController) }
                }
            )
        }
        composable<RegisterRoute> {
            RegisterScreen(
                onBack = { navController.popBackStack() },
                onRegistered = { navController.navigate(TastesRoute()) { clearStack(navController) } }
            )
        }
        composable<RecoverRoute> {
            RecoverScreen(
                onBack = { navController.popBackStack() },
                onSent = { email ->
                    navController.navigate(CheckEmailRoute(CheckEmailMode.RECOVERY.name, email))
                }
            )
        }
        composable<CheckEmailRoute> { CheckEmailScreen(onBack = { navController.popBackStack() }) }
        composable<TastesRoute> {
            TastesScreen(
                onBack = { navController.popBackStack() },
                onFinished = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(HomeRoute) { clearStack(navController) }
                    }
                }
            )
        }
        composable<HomeRoute> { HomeRouteScreen(onProfile = { navController.navigate(ProfileRoute) }) }
        composable<ProfileRoute> {
            ProfileScreen(
                onHome = { navController.popBackStack(HomeRoute, inclusive = false) },
                onEdit = { navController.navigate(EditProfileRoute) },
                onSettings = { navController.navigate(SettingsRoute) },
                onVerifyEmail = { email ->
                    navController.navigate(CheckEmailRoute(CheckEmailMode.VERIFICATION.name, email))
                }
            )
        }
        composable<EditProfileRoute> {
            EditProfileScreen(
                onBack = { navController.popBackStack() },
                onTastes = { navController.navigate(TastesRoute(fromSettings = true)) },
                onPrivacy = { navController.navigate(PrivacyRoute) },
                onDeleteAccount = { navController.navigate(DeleteAccountRoute) }
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(
                SettingsNavigation(
                    onBack = { navController.popBackStack() },
                    onPersonalData = { navController.navigate(EditProfileRoute) },
                    onTastes = { navController.navigate(TastesRoute(fromSettings = true)) },
                    onSessions = { navController.navigate(SessionsRoute) },
                    onPrivacy = { navController.navigate(PrivacyRoute) },
                    onLoggedOut = { navController.navigate(LoginRoute) { clearStack(navController) } }
                )
            )
        }
        composable<SessionsRoute> {
            SessionsScreen(
                onBack = { navController.popBackStack() },
                onAllClosed = { navController.navigate(LoginRoute) { clearStack(navController) } }
            )
        }
        composable<PrivacyRoute> { PrivacyScreen(onBack = { navController.popBackStack() }) }
        composable<DeleteAccountRoute> {
            DeleteAccountScreen(
                onBack = { navController.popBackStack() },
                onDeleted = { navController.navigate(LoginRoute) { clearStack(navController) } }
            )
        }
        composable<DeletionPendingRoute> {
            DeletionPendingScreen(
                onCancelled = { navController.navigate(HomeRoute) { clearStack(navController) } },
                onLoggedOut = { navController.navigate(LoginRoute) { clearStack(navController) } }
            )
        }
    }
}

/** Al entrar o salir de sesión no debe quedar nada atrás. */
private fun NavOptionsBuilder.clearStack(navController: NavHostController) {
    popUpTo(navController.graph.id) { inclusive = true }
    launchSingleTop = true
}
