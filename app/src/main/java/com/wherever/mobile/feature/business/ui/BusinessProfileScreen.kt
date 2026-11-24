package com.wherever.mobile.feature.business.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.wherever.mobile.R
import com.wherever.mobile.core.designsystem.ConfirmDialog
import com.wherever.mobile.core.designsystem.Danger
import com.wherever.mobile.core.designsystem.ErrorState
import com.wherever.mobile.core.designsystem.FormScreen
import com.wherever.mobile.core.designsystem.GoldTint
import com.wherever.mobile.core.designsystem.InfoBanner
import com.wherever.mobile.core.designsystem.Ink
import com.wherever.mobile.core.designsystem.ListSkeleton
import com.wherever.mobile.core.designsystem.MintTint
import com.wherever.mobile.core.designsystem.Sizes
import com.wherever.mobile.core.designsystem.Spacing
import com.wherever.mobile.core.designsystem.StatusPill
import com.wherever.mobile.core.designsystem.SunsetTint
import com.wherever.mobile.core.designsystem.SurfaceCard
import com.wherever.mobile.core.designsystem.WhereverShapes
import com.wherever.mobile.core.designsystem.WhereverSnackbarHost
import com.wherever.mobile.core.designsystem.WhereverTextLink
import com.wherever.mobile.core.designsystem.WhereverTheme
import com.wherever.mobile.feature.business.domain.BusinessProfile

/** 58_NegPerfil (specs/nv-38). Se vuelve a pedir al regresar de editar. */
@Composable
internal fun BusinessProfileRoute(
    businessId: String,
    navigation: ProfileNavigation,
    viewModel: BusinessProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LifecycleResumeEffect(businessId) {
        viewModel.start(businessId)
        onPauseOrDispose { }
    }
    LaunchedEffect(viewModel) { viewModel.exits.collect(navigation.onExit) }
    BusinessProfileContent(
        state,
        navigation,
        BusinessProfileActions(
            onRetry = viewModel::load,
            onAccepts = viewModel::onAcceptsReservations,
            onOpenPause = viewModel::openPause,
            onClosePause = viewModel::closePause,
            onPauseLength = viewModel::onPauseLength,
            onPauseReason = viewModel::onPauseReason,
            onTogglePause = viewModel::togglePause,
            onExplore = viewModel::explore,
            onLogout = viewModel::logout,
            onOpenDelete = viewModel::openDelete,
            onCloseDelete = viewModel::closeDelete,
            onDeleteText = viewModel::onDeleteText,
            onDelete = viewModel::delete,
            onMessageShown = viewModel::messageShown
        )
    )
}

@Composable
internal fun BusinessProfileContent(
    state: BusinessProfileUiState,
    navigation: ProfileNavigation,
    actions: BusinessProfileActions
) {
    val snackbar = remember { SnackbarHostState() }
    val message = state.message?.asString()
    LaunchedEffect(message) {
        if (message != null) {
            snackbar.showSnackbar(message)
            actions.onMessageShown()
        }
    }
    var confirmLogout by remember { mutableStateOf(false) }
    val error = state.error
    val profile = state.profile
    FormScreen(footer = { WhereverSnackbarHost(snackbar) }) {
        when {
            state.loading -> ListSkeleton(rows = 5)
            error != null -> ErrorState(error.asString(), actions.onRetry, Modifier.heightIn(max = Sizes.HeroImage))
            profile != null -> {
                ProfileHeader(profile)
                if (profile.paused) {
                    InfoBanner(
                        stringResource(R.string.biz_profile_paused_banner),
                        container = GoldTint,
                        contentColor = Ink,
                        tag = "biz_profile_paused"
                    )
                }
                ProfileRows(profile, state, navigation, actions)
                InfoBanner(
                    stringResource(
                        if (profile.paymentsConnected) {
                            R.string.biz_profile_payments_on
                        } else {
                            R.string.biz_profile_payments_off
                        }
                    ),
                    container = if (profile.paymentsConnected) MintTint else SunsetTint,
                    contentColor = Ink,
                    tag = "biz_profile_payments"
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    WhereverTextLink(
                        stringResource(R.string.settings_logout),
                        onClick = { confirmLogout = true },
                        enabled = !state.working,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("biz_profile_logout")
                    )
                    WhereverTextLink(
                        stringResource(R.string.biz_profile_delete),
                        onClick = actions.onOpenDelete,
                        enabled = !state.working,
                        color = Danger,
                        modifier = Modifier.testTag("biz_profile_delete")
                    )
                }
            }
        }
    }
    if (confirmLogout) {
        ConfirmDialog(
            title = stringResource(R.string.settings_logout_title),
            message = stringResource(R.string.settings_logout_message),
            confirmText = stringResource(R.string.settings_logout),
            onConfirm = {
                confirmLogout = false
                actions.onLogout()
            },
            onDismiss = { confirmLogout = false }
        )
    }
    if (state.pauseOpen && profile != null) PauseSheet(state, actions)
    if (state.deleteOpen && profile != null) DeleteBusinessSheet(state, profile.name, actions)
}

/** Portada (primera foto), nombre, Sello y dirección. */
@Composable
private fun ProfileHeader(profile: BusinessProfile) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(Sizes.HeroImage / 2)
            .clip(WhereverShapes.Card)
            .background(SunsetTint)
            .testTag("biz_profile_cover")
    ) {
        profile.photos.firstOrNull()?.let {
            AsyncImage(
                model = it.url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxWidth().height(Sizes.HeroImage / 2)
            )
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(Spacing.XS)) {
        Text(profile.name, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(Spacing.S)) {
            StatusPill(
                stringResource(profile.stateLabel()),
                container = if (profile.paused) GoldTint else MintTint,
                contentColor = Ink
            )
            profile.address?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

private fun BusinessProfile.stateLabel(): Int = when {
    paused -> R.string.biz_profile_state_paused
    state == "en_revision" -> R.string.biz_profile_state_review
    state == "aprobado" || state == "publicado" -> R.string.biz_profile_state_live
    else -> R.string.biz_profile_state_draft
}

@Composable
private fun ProfileRows(
    profile: BusinessProfile,
    state: BusinessProfileUiState,
    navigation: ProfileNavigation,
    actions: BusinessProfileActions
) {
    SurfaceCard {
        ProfileRow(Icons.Outlined.Edit, R.string.biz_profile_data, "biz_profile_data", onClick = navigation.onData)
        Divider()
        ProfileRow(
            ImageVector.vectorResource(R.drawable.ic_wh_camera),
            R.string.biz_profile_photos,
            "biz_profile_photos",
            onClick = navigation.onGallery
        )
        Divider()
        ProfileRow(
            Icons.Outlined.ShoppingCart,
            R.string.biz_profile_payments,
            "biz_profile_payments_row",
            onClick = navigation.onPayments
        )
        Divider()
        ProfileRow(Icons.Outlined.DateRange, R.string.biz_profile_reservations, "biz_profile_reservations") {
            profile.acceptsReservations?.let { accepts ->
                Switch(
                    checked = accepts,
                    onCheckedChange = actions.onAccepts,
                    enabled = !state.working,
                    modifier = Modifier.testTag("biz_profile_accepts")
                )
            }
        }
        Divider()
        ProfileRow(
            ImageVector.vectorResource(R.drawable.ic_wh_users),
            R.string.biz_profile_capacity,
            "biz_profile_capacity",
            onClick = navigation.onCapacity
        )
        if (profile.canPause) {
            Divider()
            ProfileRow(
                Icons.Outlined.Lock,
                if (profile.paused) R.string.biz_profile_resume else R.string.biz_profile_pause,
                "biz_profile_pause",
                onClick = if (profile.paused) actions.onTogglePause else actions.onOpenPause
            )
        }
        Divider()
        ProfileRow(
            ImageVector.vectorResource(R.drawable.ic_wh_compass),
            R.string.biz_profile_explore,
            "biz_profile_explore",
            onClick = actions.onExplore
        )
        Divider()
        ProfileRow(Icons.AutoMirrored.Outlined.List, R.string.biz_profile_tax, "biz_profile_tax", onClick = {
            navigation.onTax(profile.taxDataComplete)
        })
    }
}

@Composable
private fun Divider() = HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

/** Fila con ícono: con [onClick] lleva flecha; si no, lo de la derecha ([trailing]). */
@Composable
private fun ProfileRow(
    icon: ImageVector,
    title: Int,
    tag: String,
    onClick: (() -> Unit)? = null,
    trailing: @Composable () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = Sizes.RowMinHeight)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = Spacing.L, vertical = Spacing.S)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(
                Sizes.IconXL
            ).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(Sizes.IconS))
        }
        Spacer(Modifier.width(Spacing.M))
        Text(stringResource(title), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        if (onClick != null) {
            Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, contentDescription = null)
        } else {
            trailing()
        }
    }
}

@Preview(name = "58_NegPerfil claro")
@Preview(name = "58_NegPerfil oscuro", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
internal fun BusinessProfilePreview() {
    WhereverTheme {
        BusinessProfileContent(
            BusinessProfileUiState(loading = false, profile = sampleBusinessProfile()),
            ProfileNavigation(),
            BusinessProfileActions()
        )
    }
}

@Preview(name = "58_NegPerfil eliminar")
@Composable
internal fun BusinessDeletePreview() {
    WhereverTheme {
        Column(modifier = Modifier.padding(Spacing.L)) {
            DeleteBusinessForm(
                BusinessProfileUiState(
                    loading = false,
                    profile = sampleBusinessProfile(),
                    deleteOpen = true,
                    deleteText = "Café"
                ),
                sampleBusinessProfile().name,
                BusinessProfileActions()
            )
        }
    }
}

/** Texto centrado de las hojas de 58. */
@Composable
internal fun SheetTitle(title: Int, body: String) {
    Text(stringResource(title), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text(
        body,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Start
    )
}
