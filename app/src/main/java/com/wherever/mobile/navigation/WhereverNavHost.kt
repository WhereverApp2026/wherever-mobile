package com.wherever.mobile.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.wherever.mobile.BuildConfig
import com.wherever.mobile.feature.auth.data.RegistrationOutcome
import com.wherever.mobile.feature.auth.domain.OnboardingStep
import com.wherever.mobile.feature.auth.ui.AuthGateScreen
import com.wherever.mobile.feature.auth.ui.AuthGateViewModel
import com.wherever.mobile.feature.auth.ui.FriendsScreen
import com.wherever.mobile.feature.auth.ui.LocationScreen
import com.wherever.mobile.feature.auth.ui.LoginScreen
import com.wherever.mobile.feature.auth.ui.OnboardingStartScreen
import com.wherever.mobile.feature.auth.ui.PermissionsScreen
import com.wherever.mobile.feature.auth.ui.PreferencesScreen
import com.wherever.mobile.feature.auth.ui.RecoverScreen
import com.wherever.mobile.feature.auth.ui.RecoverSentScreen
import com.wherever.mobile.feature.auth.ui.RegisterScreen
import com.wherever.mobile.feature.auth.ui.ResetPasswordScreen
import com.wherever.mobile.feature.auth.ui.SessionsScreen
import com.wherever.mobile.feature.auth.ui.VerifyEmailScreen
import com.wherever.mobile.feature.auth.ui.WelcomeScreen
import com.wherever.mobile.ui.debug.DebugConnectionScreen
import com.wherever.mobile.ui.home.HomeScreen

@Suppress("FunctionNaming")
@Composable
fun WhereverNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: Any = AuthGateRoute,
    debugScreen: @Composable () -> Unit = { DebugConnectionScreen() }
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            authEntryDestinations(navController)
            authRegistrationDestinations(navController)
            authRecoveryDestinations(navController)
            authSessionDestinations(navController)
            onboardingDestinations(navController)
            composable<HomeRoute> {
                HomeScreen()
            }
            if (BuildConfig.DEBUG) {
                composable<DebugConnectionRoute> {
                    debugScreen()
                }
            }
        }
    }
}

private fun NavGraphBuilder.authEntryDestinations(navController: NavHostController) {
    composable<AuthGateRoute> {
        val viewModel: AuthGateViewModel = hiltViewModel()
        val state by viewModel.state.collectAsStateWithLifecycle()
        AuthGateScreen(
            state = state,
            onUnauthenticated = {
                navController.navigate(AuthWelcomeRoute) {
                    popUpTo<AuthGateRoute> { inclusive = true }
                }
            },
            onAuthenticated = {
                navController.navigate(AuthOnboardingRoute) {
                    popUpTo<AuthGateRoute> { inclusive = true }
                }
            }
        )
    }
    composable<AuthWelcomeRoute> {
        WelcomeScreen(
            onCreateAccount = { navController.navigate(AuthRegisterRoute) },
            onSignIn = { navController.navigate(AuthLoginRoute) }
        )
    }
    composable<AuthLoginRoute> {
        LoginScreen(
            onLoggedIn = {
                navController.navigate(AuthOnboardingRoute) {
                    popUpTo<AuthWelcomeRoute> { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
            onCreateAccount = { navController.navigate(AuthRegisterRoute) },
            onForgotPassword = { navController.navigate(AuthRecoverRoute) },
            onResetPassword = { navController.navigate(AuthRecoverResetRoute) }
        )
    }
}

private fun NavGraphBuilder.authRegistrationDestinations(navController: NavHostController) {
    composable<AuthRegisterRoute> {
        RegisterScreen(
            onRegistered = { outcome ->
                val destination = when (outcome) {
                    RegistrationOutcome.SESSION_CREATED -> AuthOnboardingRoute
                    RegistrationOutcome.VERIFICATION_REQUIRED -> AuthVerifyEmailRoute
                }
                navController.navigate(destination) {
                    popUpTo<AuthRegisterRoute> { inclusive = true }
                }
            },
            onBack = { navController.popBackStack() },
            onSignIn = { navController.navigate(AuthLoginRoute) }
        )
    }
    composable<AuthVerifyEmailRoute> {
        VerifyEmailScreen(
            onVerified = { navController.navigateToLoginFromAuthFlow() },
            onBack = { navController.navigateToLoginFromAuthFlow() }
        )
    }
}

private fun NavGraphBuilder.authRecoveryDestinations(navController: NavHostController) {
    composable<AuthRecoverRoute> {
        RecoverScreen(
            onSent = { navController.navigate(AuthRecoverSentRoute) },
            onBack = { navController.popBackStack() },
            onOpenReset = { navController.navigate(AuthRecoverResetRoute) }
        )
    }
    composable<AuthRecoverSentRoute> {
        RecoverSentScreen(
            onOpenReset = { navController.navigate(AuthRecoverResetRoute) },
            onBackToLogin = { navController.navigateToLoginFromAuthFlow() }
        )
    }
    composable<AuthRecoverResetRoute> {
        ResetPasswordScreen(
            onReset = { navController.navigateToLoginFromAuthFlow() },
            onBack = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.authSessionDestinations(navController: NavHostController) {
    composable<AuthSessionsRoute> {
        SessionsScreen(
            onSessionExpired = { navController.navigateToWelcome() },
            onLoggedOut = { navController.navigateToWelcome() }
        )
    }
}

private fun NavGraphBuilder.onboardingDestinations(navController: NavHostController) {
    composable<AuthOnboardingRoute> {
        OnboardingStartScreen(
            onStep = { step ->
                val destination = onboardingDestination(step)
                if (destination == null) {
                    navController.navigateToSessionsFromOnboarding()
                } else {
                    navController.navigate(destination)
                }
            }
        )
    }
    composable<AuthRegisterPreferencesRoute> {
        PreferencesScreen(
            onContinue = { navController.navigate(AuthRegisterLocationRoute) }
        )
    }
    composable<AuthRegisterLocationRoute> {
        LocationScreen(
            onContinue = { navController.navigate(AuthRegisterFriendsRoute) }
        )
    }
    composable<AuthRegisterFriendsRoute> {
        FriendsScreen(
            onContinue = { navController.navigate(AuthRegisterPermissionsRoute) }
        )
    }
    composable<AuthRegisterPermissionsRoute> {
        PermissionsScreen(
            onComplete = { navController.navigateToSessionsFromOnboarding() }
        )
    }
}

private fun onboardingDestination(step: OnboardingStep): Any? = when (step) {
    OnboardingStep.REGISTER, OnboardingStep.PREFERENCES -> AuthRegisterPreferencesRoute
    OnboardingStep.LOCATION -> AuthRegisterLocationRoute
    OnboardingStep.FRIENDS -> AuthRegisterFriendsRoute
    OnboardingStep.PERMISSIONS -> AuthRegisterPermissionsRoute
    OnboardingStep.COMPLETE -> null
}

private fun NavHostController.navigateToSessionsFromOnboarding() {
    navigate(AuthSessionsRoute) {
        popUpTo(graph.id) { inclusive = true }
    }
}

private fun NavHostController.navigateToLoginFromAuthFlow() {
    navigate(AuthLoginRoute) {
        popUpTo<AuthWelcomeRoute>()
    }
}

private fun NavHostController.navigateToWelcome() {
    navigate(AuthWelcomeRoute) {
        popUpTo(graph.id) { inclusive = true }
    }
}
