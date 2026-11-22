package com.wherever.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.wherever.mobile.core.designsystem.LocalMotionEnabled
import com.wherever.mobile.core.designsystem.MainTab
import com.wherever.mobile.core.designsystem.NavMotion
import com.wherever.mobile.core.designsystem.SystemBarIcons
import com.wherever.mobile.core.notifications.PushDestination
import com.wherever.mobile.feature.account.ui.ChangeEmailRoute as ChangeEmailScreen
import com.wherever.mobile.feature.account.ui.ChangePasswordRoute as ChangePasswordScreen
import com.wherever.mobile.feature.account.ui.MyDataRoute as MyDataScreen
import com.wherever.mobile.feature.auth.domain.EmailLink
import com.wherever.mobile.feature.auth.domain.PostAuthDestination
import com.wherever.mobile.feature.auth.ui.CheckEmailMode
import com.wherever.mobile.feature.auth.ui.CheckEmailRoute as CheckEmailScreen
import com.wherever.mobile.feature.auth.ui.LoginRoute as LoginScreen
import com.wherever.mobile.feature.auth.ui.NewPasswordRoute as NewPasswordScreen
import com.wherever.mobile.feature.auth.ui.PermissionsRoute as PermissionsScreen
import com.wherever.mobile.feature.auth.ui.RecoverRoute as RecoverScreen
import com.wherever.mobile.feature.auth.ui.RegisterRoute as RegisterScreen
import com.wherever.mobile.feature.auth.ui.VerifyEmailRoute as VerifyEmailScreen
import com.wherever.mobile.feature.auth.ui.WelcomeScreen
import com.wherever.mobile.feature.feed.ui.FriendPlanNavigation
import com.wherever.mobile.feature.feed.ui.FriendPlanRoute as FriendPlanScreen
import com.wherever.mobile.feature.feed.ui.MomentDetailRoute
import com.wherever.mobile.feature.feed.ui.MomentNavigation
import com.wherever.mobile.feature.friends.ui.AddFriendRoute as AddFriendScreen
import com.wherever.mobile.feature.friends.ui.FriendsRoute as FriendsScreen
import com.wherever.mobile.feature.friends.ui.MyCodeRoute as MyCodeScreen
import com.wherever.mobile.feature.friends.ui.PersonRoute as PersonScreen
import com.wherever.mobile.feature.groups.ui.GroupChatRoute as GroupChatScreen
import com.wherever.mobile.feature.groups.ui.MyGroupsRoute as MyGroupsScreen
import com.wherever.mobile.feature.home.ui.HomeNavigation
import com.wherever.mobile.feature.home.ui.HomeRouteScreen
import com.wherever.mobile.feature.memories.ui.AlbumNavigation
import com.wherever.mobile.feature.memories.ui.AlbumRoute as AlbumScreen
import com.wherever.mobile.feature.memories.ui.CameraRoute as CameraScreen
import com.wherever.mobile.feature.memories.ui.CreateMomentNavigation
import com.wherever.mobile.feature.memories.ui.CreateMomentRoute as CreateMomentScreen
import com.wherever.mobile.feature.memories.ui.PostcardDetailRoute as PostcardDetailScreen
import com.wherever.mobile.feature.memories.ui.PostcardEditorRoute as PostcardEditorScreen
import com.wherever.mobile.feature.notifications.ui.NotificationsRoute as NotificationsScreen
import com.wherever.mobile.feature.places.ui.ExploreRoute as ExploreScreen
import com.wherever.mobile.feature.places.ui.FavoritesRoute as FavoritesScreen
import com.wherever.mobile.feature.places.ui.PlaceDetailRoute as PlaceDetailScreen
import com.wherever.mobile.feature.plans.ui.CreatePlanRoute as CreatePlanScreen
import com.wherever.mobile.feature.plans.ui.MyPlansRoute as MyPlansScreen
import com.wherever.mobile.feature.plans.ui.PlanDetailRoute as PlanDetailScreen
import com.wherever.mobile.feature.profile.ui.BadgesRoute as BadgesScreen
import com.wherever.mobile.feature.profile.ui.EditProfileRoute as EditProfileScreen
import com.wherever.mobile.feature.profile.ui.ProfileRoute as ProfileScreen
import com.wherever.mobile.feature.reservations.ui.MyReservationsRoute as MyReservationsScreen
import com.wherever.mobile.feature.reservations.ui.ReservationRoute as ReservationScreen
import com.wherever.mobile.feature.reservations.ui.ReserveRoute as ReserveScreen
import com.wherever.mobile.feature.reviews.domain.PendingReview
import com.wherever.mobile.feature.reviews.ui.MyReviewsRoute as MyReviewsScreen
import com.wherever.mobile.feature.reviews.ui.PendingReviewsRoute as PendingReviewsScreen
import com.wherever.mobile.feature.reviews.ui.PlaceReviewsRoute as PlaceReviewsScreen
import com.wherever.mobile.feature.reviews.ui.ReviewFormNavigation
import com.wherever.mobile.feature.reviews.ui.ReviewFormRoute as ReviewFormScreen
import com.wherever.mobile.feature.search.ui.SearchActions
import com.wherever.mobile.feature.search.ui.SearchRoute as SearchScreen
import com.wherever.mobile.feature.settings.ui.DeleteAccountRoute as DeleteAccountScreen
import com.wherever.mobile.feature.settings.ui.DeletionPendingRoute as DeletionPendingScreen
import com.wherever.mobile.feature.settings.ui.PrivacyRoute as PrivacyScreen
import com.wherever.mobile.feature.settings.ui.SessionsRoute as SessionsScreen
import com.wherever.mobile.feature.settings.ui.SettingsNavigation
import com.wherever.mobile.feature.settings.ui.SettingsRoute as SettingsScreen
import com.wherever.mobile.feature.tastes.ui.TastesRoute as TastesScreen
import java.time.LocalDate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun WhereverNavHost(
    start: StartDestination,
    rootEvents: Flow<RootEvent>,
    pushes: Flow<PushDestination>,
    links: Flow<EmailLink> = emptyFlow()
) {
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
    LaunchedEffect(navController) {
        links.collect { link ->
            navController.navigate(link.route()) { launchSingleTop = true }
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
                    navController.navigateOnResult(e, destination.route()) { clearStack(navController) }
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
        ) { e ->
            CheckEmailScreen(
                onBack = { navController.popFrom(e) },
                onLink = { link -> navController.navigateFrom(e, link.route()) }
            )
        }
        composable<NewPasswordRoute> { e ->
            NewPasswordScreen(
                onDone = { destination ->
                    navController.navigateOnResult(e, destination.route()) { clearStack(navController) }
                },
                onRequestAnother = { navController.navigateFrom(e, RecoverRoute) }
            )
        }
        composable<VerifyEmailRoute> { e ->
            VerifyEmailScreen(onContinue = {
                if (!navController.popBackStack()) navController.navigate(WelcomeRoute) { clearStack(navController) }
            })
        }
        composable<TastesRoute> { e ->
            TastesScreen(
                onBack = { navController.popFrom(e) },
                onFinished = {
                    if (navController.previousBackStackEntry != null) {
                        navController.popOnResult(e)
                    } else {
                        // Alta: Gustos → Permisos (paso 3 de 3); "atrás" en Permisos regresa a Gustos.
                        navController.navigateOnResult(e, PermissionsRoute)
                    }
                }
            )
        }
        composable<PermissionsRoute> { e ->
            PermissionsScreen(
                onBack = { navController.popFrom(e) },
                onFinished = { navController.navigateOnResult(e, HomeRoute) { clearStack(navController) } }
            )
        }
        composable<HomeRoute> { e ->
            HomeRouteScreen(
                HomeNavigation(
                    onTab = { navController.openTab(e, it) },
                    onSearch = { navController.navigateFrom(e, SearchRoute()) },
                    onPlan = { navController.navigateFrom(e, PlanRoute(it)) },
                    onCategory = { navController.navigateFrom(e, ExploreRoute(category = it)) },
                    onPlace = { navController.navigateFrom(e, PlaceRoute(it, origin = "inicio")) },
                    onNotifications = { navController.navigateFrom(e, NotificationsRoute) },
                    onMoment = { navController.navigateFrom(e, MomentRoute(it)) },
                    onFriendPlan = { navController.navigateFrom(e, FriendPlanRoute(it)) },
                    onPerson = { navController.navigateFrom(e, PersonRoute(it)) },
                    onAddFriends = { navController.navigateFrom(e, AddFriendRoute) }
                )
            )
        }
        composable<MomentRoute> { e ->
            MomentDetailRoute(
                MomentNavigation(
                    onBack = { navController.popFrom(e) },
                    onPlace = { navController.navigateFrom(e, PlaceRoute(it, origin = "inicio")) },
                    onPlan = { navController.navigateFrom(e, FriendPlanRoute(it)) }
                )
            )
        }
        composable<FriendPlanRoute> { e ->
            FriendPlanScreen(
                FriendPlanNavigation(
                    onBack = { navController.popFrom(e) },
                    onMyCopy = { navController.navigateFrom(e, PlanRoute(it)) }
                )
            )
        }
        composable<SearchRoute> { e ->
            SearchScreen(
                SearchActions(
                    onBack = { navController.popFrom(e) },
                    onPlace = { navController.navigateFrom(e, PlaceRoute(it, origin = "busqueda")) },
                    onPerson = { navController.navigateFrom(e, PersonRoute(it)) },
                    onPlan = { navController.navigateFrom(e, PlanRoute(it)) },
                    onAskAi = { navController.navigateFrom(e, CreatePlanRoute(phrase = it)) },
                    onExplore = { navController.navigateFrom(e, ExploreRoute()) }
                )
            )
        }
        composable<ExploreRoute> { e ->
            ExploreScreen(
                onTab = { navController.openTab(e, it) },
                onPlace = { navController.navigateFrom(e, PlaceRoute(it, origin = "busqueda")) }
            )
        }
        composable<PlaceRoute> { e ->
            PlaceDetailScreen(
                onBack = { navController.popFrom(e) },
                onReserve = { id, name -> navController.navigateFrom(e, ReserveRoute(id, name)) },
                onReviews = { id, name -> navController.navigateFrom(e, PlaceReviewsRoute(id, name)) }
            )
        }
        composable<PlaceReviewsRoute> { e -> PlaceReviewsScreen(onBack = { navController.popFrom(e) }) }
        memoriesGraph(navController)
        composable<PendingReviewsRoute> { e ->
            PendingReviewsScreen(
                onBack = { navController.popFrom(e) },
                onReview = { navController.navigateFrom(e, it.formRoute()) }
            )
        }
        composable<MyReviewsRoute> { e ->
            MyReviewsScreen(
                onBack = { navController.popFrom(e) },
                onEdit = { navController.navigateFrom(e, ReviewFormRoute(placeId = it.placeId, reviewId = it.id)) },
                onReview = { navController.navigateFrom(e, it.formRoute()) }
            )
        }
        composable<ReviewFormRoute> { e ->
            ReviewFormScreen(
                ReviewFormNavigation(
                    onClose = { navController.popFrom(e) },
                    onMyReviews = {
                        navController.navigateOnResult(e, MyReviewsRoute) {
                            popUpTo<ReviewFormRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onHome = { navController.openTab(e, MainTab.HOME) },
                    onRepeat = { place ->
                        navController.navigateOnResult(e, CreatePlanRoute(place)) {
                            popUpTo<ReviewFormRoute> { inclusive = true }
                        }
                    }
                )
            )
        }
        composable<ReserveRoute> { e ->
            ReserveScreen(
                onBack = { navController.popFrom(e) },
                // La confirmación reemplaza a Reservar: regresar vuelve a la ficha, no al formulario.
                onCreated = { id ->
                    navController.navigateOnResult(e, ReservationRoute(id)) {
                        popUpTo<ReserveRoute> { inclusive = true }
                    }
                }
            )
        }
        composable<ReservationRoute> { e -> ReservationScreen(onBack = { navController.popFrom(e) }) }
        composable<MyReservationsRoute> { e ->
            MyReservationsScreen(
                onBack = { navController.popFrom(e) },
                onReservation = { navController.navigateFrom(e, ReservationRoute(it)) }
            )
        }
        composable<CreatePlanRoute> { e ->
            CreatePlanScreen(
                onBack = { navController.popFrom(e) },
                onSaved = { id ->
                    navController.navigateOnResult(e, PlanRoute(id)) { popUpTo<CreatePlanRoute> { inclusive = true } }
                }
            )
        }
        composable<PlanRoute> { e ->
            PlanDetailScreen(
                onBack = { navController.popOnResult(e) },
                onOpenPlan = { id -> navController.navigateOnResult(e, PlanRoute(id)) }
            )
        }
        composable<MyPlansRoute> { e ->
            MyPlansScreen(
                onTab = { navController.openTab(e, it) },
                onPlan = { navController.navigateFrom(e, PlanRoute(it)) },
                onCreate = { navController.navigateFrom(e, CreatePlanRoute()) },
                onGroups = { navController.navigateFrom(e, MyGroupsRoute) }
            )
        }
        composable<MyGroupsRoute> { e ->
            MyGroupsScreen(
                onBack = { navController.popFrom(e) },
                onGroup = { navController.navigateFrom(e, GroupChatRoute(it)) },
                // Un grupo nace de un plan (POST /grupos con plan_id): se elige el plan en Planes.
                onNewGroup = { navController.openTab(e, MainTab.PLANS) }
            )
        }
        composable<GroupChatRoute> { e ->
            GroupChatScreen(
                onBack = { navController.popFrom(e) },
                onPlan = { navController.navigateFrom(e, PlanRoute(it)) },
                onGone = { navController.popOnResult(e) }
            )
        }
        composable<FavoritesRoute> { e ->
            FavoritesScreen(
                onBack = { navController.popFrom(e) },
                onPlace = { navController.navigateFrom(e, PlaceRoute(it)) }
            )
        }
        composable<NotificationsRoute> { e ->
            NotificationsScreen(
                onBack = { navController.popFrom(e) },
                onOpen = { push ->
                    val route = push.route()
                    if (route != NotificationsRoute) navController.navigateFrom(e, route)
                }
            )
        }
        composable<FriendsRoute> { e ->
            FriendsScreen(
                onBack = { navController.popFrom(e) },
                onAdd = { navController.navigateFrom(e, AddFriendRoute) },
                onPerson = { navController.navigateFrom(e, PersonRoute(it)) },
                onGroups = { navController.navigateFrom(e, MyGroupsRoute) }
            )
        }
        composable<AddFriendRoute> { e ->
            AddFriendScreen(
                onBack = { navController.popFrom(e) },
                onPerson = { navController.navigateFrom(e, PersonRoute(it)) },
                onMyCode = { scan -> navController.navigateFrom(e, MyCodeRoute(scan)) }
            )
        }
        composable<MyCodeRoute> { e ->
            MyCodeScreen(
                onBack = { navController.popFrom(e) },
                onPerson = { navController.navigateFrom(e, PersonRoute(it)) }
            )
        }
        composable<PersonRoute> { e ->
            PersonScreen(onBack = { navController.popFrom(e) }, onBlocked = { navController.popOnResult(e) })
        }
        composable<ProfileRoute> { e ->
            ProfileScreen(
                onTab = { navController.openTab(e, it) },
                onEdit = { navController.navigateFrom(e, EditProfileRoute) },
                onFavorites = { navController.navigateFrom(e, FavoritesRoute) },
                onFriends = { navController.navigateFrom(e, FriendsRoute) },
                onReservations = { navController.navigateFrom(e, MyReservationsRoute) },
                onBadges = { navController.navigateFrom(e, BadgesRoute) },
                onMyReviews = { navController.navigateFrom(e, MyReviewsRoute) },
                onPendingReviews = { navController.navigateFrom(e, PendingReviewsRoute) },
                onMemories = { navController.navigateFrom(e, AlbumRoute) },
                onNewMoment = { navController.navigateFrom(e, CreateMomentRoute()) },
                onSettings = { navController.navigateFrom(e, SettingsRoute) },
                onVerifyEmail = { email ->
                    navController.navigateFrom(e, CheckEmailRoute(CheckEmailMode.VERIFICATION.name, email))
                }
            )
        }
        composable<BadgesRoute> { e ->
            BadgesScreen(onBack = { navController.popFrom(e) })
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
                    onChangeEmail = { navController.navigateFrom(e, ChangeEmailRoute) },
                    onChangePassword = { navController.navigateFrom(e, ChangePasswordRoute) },
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
        composable<PrivacyRoute> { e ->
            PrivacyScreen(
                onBack = { navController.popFrom(e) },
                onMyData = { navController.navigateFrom(e, MyDataRoute) }
            )
        }
        composable<ChangePasswordRoute> { e -> ChangePasswordScreen(onBack = { navController.popFrom(e) }) }
        composable<ChangeEmailRoute> { e -> ChangeEmailScreen(onBack = { navController.popFrom(e) }) }
        composable<MyDataRoute> { e -> MyDataScreen(onBack = { navController.popFrom(e) }) }
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

/** Las pestañas comparten la raíz Inicio y guardan su estado (scroll, filtros) al cambiar. */
private fun NavHostController.openTab(from: NavBackStackEntry, tab: MainTab) {
    val route: Any = when (tab) {
        MainTab.HOME -> HomeRoute
        MainTab.EXPLORE -> ExploreRoute()
        MainTab.PLANS -> MyPlansRoute
        MainTab.PROFILE -> ProfileRoute
    }
    navigateFrom(from, route) {
        popUpTo<HomeRoute> { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}

/** Al entrar o salir de sesión no debe quedar nada atrás. */
private fun NavOptionsBuilder.clearStack(navController: NavHostController) {
    popUpTo(navController.graph.id) { inclusive = true }
    launchSingleTop = true
}

private fun PostAuthDestination.route(): Any = when (this) {
    PostAuthDestination.HOME -> HomeRoute
    PostAuthDestination.TASTES -> TastesRoute()
    PostAuthDestination.DELETION_PENDING -> DeletionPendingRoute
}

private fun EmailLink.route(): Any = when (this) {
    is EmailLink.ResetPassword -> NewPasswordRoute
    is EmailLink.VerifyEmail -> VerifyEmailRoute
}

/** Reseñar una visita pendiente (77_ResPend y la pestaña Por reseñar de 79_MisRes). */
private fun PendingReview.formRoute() = ReviewFormRoute(
    checkinId = checkinId,
    placeId = placeId,
    placeName = placeName,
    day = day
)

/** Recuerdos y postales (specs/nv-31): álbum, cámara, postal, detalle y Nuevo momento. */
private fun NavGraphBuilder.memoriesGraph(navController: NavHostController) {
    composable<AlbumRoute> { e ->
        AlbumScreen(
            AlbumNavigation(
                onBack = { navController.popFrom(e) },
                onPostcard = { navController.navigateFrom(e, PostcardRoute(it)) },
                onCamera = { navController.navigateFrom(e, CameraRoute()) },
                onPlans = { navController.openTab(e, MainTab.PLANS) }
            )
        )
    }
    composable<CameraRoute> { e ->
        val route = e.toRoute<CameraRoute>()
        CameraScreen(
            placeName = route.placeName,
            onClose = { navController.popFrom(e) },
            onPicked = { source ->
                val editor =
                    PostcardEditorRoute(
                        source,
                        route.placeId,
                        route.placeName,
                        route.planId,
                        LocalDate.now().toString()
                    )
                navController.navigateOnResult(e, editor) { popUpTo<CameraRoute> { inclusive = true } }
            }
        )
    }
    composable<PostcardEditorRoute> { e ->
        PostcardEditorScreen(
            onBack = { navController.popFrom(e) },
            onSaved = {
                navController.navigateOnResult(e, AlbumRoute) {
                    popUpTo<PostcardEditorRoute> { inclusive = true }
                    launchSingleTop = true
                }
            }
        )
    }
    composable<PostcardRoute> { e -> PostcardDetailScreen(onBack = { navController.popFrom(e) }) }
    composable<CreateMomentRoute> { e ->
        CreateMomentScreen(
            CreateMomentNavigation(
                onCancel = { navController.popFrom(e) },
                onPublished = { navController.popFrom(e) },
                onCamera = { navController.navigateFrom(e, CameraRoute()) },
                onReviews = { navController.navigateFrom(e, PendingReviewsRoute) }
            )
        )
    }
}
