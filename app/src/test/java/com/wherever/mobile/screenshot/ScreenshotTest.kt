package com.wherever.mobile.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.RoborazziOptions
import com.github.takahirom.roborazzi.captureRoboImage
import com.wherever.mobile.feature.auth.ui.CheckEmailPreview
import com.wherever.mobile.feature.auth.ui.LoginPreview
import com.wherever.mobile.feature.auth.ui.NewPasswordExpiredPreview
import com.wherever.mobile.feature.auth.ui.NewPasswordPreview
import com.wherever.mobile.feature.auth.ui.PermissionsPreview
import com.wherever.mobile.feature.auth.ui.RecoverPreview
import com.wherever.mobile.feature.auth.ui.RegisterPreview
import com.wherever.mobile.feature.auth.ui.WelcomePreview
import com.wherever.mobile.feature.business.ui.BusinessDonePreview
import com.wherever.mobile.feature.business.ui.BusinessLocationPreview
import com.wherever.mobile.feature.business.ui.BusinessMissingPreview
import com.wherever.mobile.feature.business.ui.BusinessMomentsPreview
import com.wherever.mobile.feature.business.ui.BusinessOperationPreview
import com.wherever.mobile.feature.business.ui.BusinessPanelPreview
import com.wherever.mobile.feature.business.ui.BusinessRegisterPreview
import com.wherever.mobile.feature.business.ui.BusinessReservationsEmptyPreview
import com.wherever.mobile.feature.business.ui.BusinessReservationsPreview
import com.wherever.mobile.feature.business.ui.BusinessReviewsPreview
import com.wherever.mobile.feature.business.ui.BusinessStatsPreview
import com.wherever.mobile.feature.business.ui.BusinessVerifyPreview
import com.wherever.mobile.feature.business.ui.CapacityPreview
import com.wherever.mobile.feature.business.ui.CheckinEmptyPreview
import com.wherever.mobile.feature.business.ui.CheckinPreview
import com.wherever.mobile.feature.business.ui.CouponFormPreview
import com.wherever.mobile.feature.business.ui.CouponsEmptyPreview
import com.wherever.mobile.feature.business.ui.CouponsPreview
import com.wherever.mobile.feature.business.ui.ReviewReplyPreview
import com.wherever.mobile.feature.checkin.ui.ArrivalOkPreview
import com.wherever.mobile.feature.checkin.ui.ArrivalPermissionPreview
import com.wherever.mobile.feature.feed.ui.FriendPlanPreview
import com.wherever.mobile.feature.feed.ui.FriendsFeedEmptyPreview
import com.wherever.mobile.feature.feed.ui.FriendsFeedPreview
import com.wherever.mobile.feature.feed.ui.MomentDetailPreview
import com.wherever.mobile.feature.friends.ui.FindFriendsPreview
import com.wherever.mobile.feature.friends.ui.MyCodePreview
import com.wherever.mobile.feature.friends.ui.MyCodeScanPreview
import com.wherever.mobile.feature.groups.ui.CreateGroupPreview
import com.wherever.mobile.feature.groups.ui.GroupChatPreview
import com.wherever.mobile.feature.groups.ui.GroupDetailPreview
import com.wherever.mobile.feature.groups.ui.InvitePlanPreview
import com.wherever.mobile.feature.groups.ui.InviteToGroupPreview
import com.wherever.mobile.feature.groups.ui.MyGroupsPreview
import com.wherever.mobile.feature.groups.ui.VotePreview
import com.wherever.mobile.feature.help.ui.HelpNoResultsPreview
import com.wherever.mobile.feature.help.ui.HelpPreview
import com.wherever.mobile.feature.help.ui.HelpReportPreview
import com.wherever.mobile.feature.home.ui.HomePreview
import com.wherever.mobile.feature.memories.ui.AlbumByPlanPreview
import com.wherever.mobile.feature.memories.ui.AlbumEmptyPreview
import com.wherever.mobile.feature.memories.ui.AlbumPreview
import com.wherever.mobile.feature.memories.ui.CameraPreview
import com.wherever.mobile.feature.memories.ui.CreateMomentPreview
import com.wherever.mobile.feature.memories.ui.PostcardDetailPassportPreview
import com.wherever.mobile.feature.memories.ui.PostcardDetailPolaroidPreview
import com.wherever.mobile.feature.memories.ui.PostcardDetailPreview
import com.wherever.mobile.feature.memories.ui.PostcardEditorPreview
import com.wherever.mobile.feature.memories.ui.PostcardGonePreview
import com.wherever.mobile.feature.plans.ui.ManualPlanPreview
import com.wherever.mobile.feature.plans.ui.PlanAddStopPreview
import com.wherever.mobile.feature.plans.ui.PlanAdjustPreview
import com.wherever.mobile.feature.plans.ui.PlanChangePreview
import com.wherever.mobile.feature.plans.ui.PlanDetailPreview
import com.wherever.mobile.feature.plans.ui.PlanFamilyPreview
import com.wherever.mobile.feature.plans.ui.PlanGeneratingPreview
import com.wherever.mobile.feature.plans.ui.PlanGeneratingStep3Preview
import com.wherever.mobile.feature.plans.ui.PlanReadyPreview
import com.wherever.mobile.feature.plans.ui.PlanWordPreview
import com.wherever.mobile.feature.profile.ui.BadgesPreview
import com.wherever.mobile.feature.profile.ui.EditProfilePreview
import com.wherever.mobile.feature.profile.ui.ProfilePreview
import com.wherever.mobile.feature.reviews.ui.MyReviewsConfirmPreview
import com.wherever.mobile.feature.reviews.ui.MyReviewsPreview
import com.wherever.mobile.feature.reviews.ui.PendingReviewsEmptyPreview
import com.wherever.mobile.feature.reviews.ui.PendingReviewsPreview
import com.wherever.mobile.feature.reviews.ui.PlaceReviewsPreview
import com.wherever.mobile.feature.reviews.ui.ReviewDetailPreview
import com.wherever.mobile.feature.reviews.ui.ReviewFormPreview
import com.wherever.mobile.feature.reviews.ui.ReviewPublishedPreview
import com.wherever.mobile.feature.settings.ui.DeleteAccountPreview
import com.wherever.mobile.feature.settings.ui.PrivacyPreview
import com.wherever.mobile.feature.settings.ui.SettingsPreview
import com.wherever.mobile.feature.status.ui.MaintenancePreview
import com.wherever.mobile.feature.status.ui.OfflineNoticePreview
import com.wherever.mobile.feature.status.ui.OfflinePreview
import com.wherever.mobile.feature.status.ui.UpdateRequiredPreview
import com.wherever.mobile.feature.tastes.ui.TastesPreview
import com.wherever.mobile.feature.vaca.ui.AddExpenseCustomPreview
import com.wherever.mobile.feature.vaca.ui.AddExpensePreview
import com.wherever.mobile.feature.vaca.ui.VacaAllPaidPreview
import com.wherever.mobile.feature.vaca.ui.VacaEmptyPreview
import com.wherever.mobile.feature.vaca.ui.VacaOnePaidPreview
import com.wherever.mobile.feature.vaca.ui.VacaPreview
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

private const val SDK = 35
private const val SMALL_PHONE = "w320dp-h568dp-xhdpi"
private const val LARGE_FONT = 1.3f

/**
 * Capturas de las pantallas principales (specs/nv-55, docs/diseno.md §7). Cada una se compara con su PNG en
 * `src/test/screenshots`; si el diseño cambia, la prueba falla. Grabar a propósito: `-Proborazzi.record=true`.
 * Variantes: claro (teléfono normal), oscuro, y pantalla chica con letra grande.
 */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [SDK], qualifiers = RobolectricDeviceQualifiers.Pixel7)
class ScreenshotTest {
    @get:Rule
    val compose = createComposeRule()

    private val screens: List<Pair<String, @Composable () -> Unit>> = listOf(
        "01_bienvenida" to { WelcomePreview() },
        "03_login" to { LoginPreview() },
        "03_recuperar" to { RecoverPreview() },
        "04_revisa_correo" to { CheckEmailPreview() },
        "nueva_contrasena" to { NewPasswordPreview() },
        "nueva_contrasena_vencida" to { NewPasswordExpiredPreview() },
        "05_registro" to { RegisterPreview() },
        "06_gustos" to { TastesPreview() },
        "08_permisos" to { PermissionsPreview() },
        "perfil" to { ProfilePreview() },
        "editar_perfil" to { EditProfilePreview() },
        "85_ajustes" to { SettingsPreview() },
        "28_priv_social" to { PrivacyPreview() },
        "eliminar_cuenta" to { DeleteAccountPreview() },
        "10_inicio" to { HomePreview() },
        "11_inicio_amigos" to { FriendsFeedPreview() },
        "11_inicio_amigos_vacio" to { FriendsFeedEmptyPreview() },
        "17_post_detalle" to { MomentDetailPreview() },
        "29_plan_amigo" to { FriendPlanPreview() },
        "25_mi_codigo" to { MyCodePreview() },
        "07_encuentra_amigos" to { FindFriendsPreview() },
        "83_insignias" to { BadgesPreview() },
        "44_mis_grupos" to { MyGroupsPreview() },
        "45_grupo_chat" to { GroupChatPreview() },
        "46_grupo" to { GroupDetailPreview() },
        "48_votar" to { VotePreview() },
        "43_crear_grupo" to { CreateGroupPreview() },
        "invitar_al_grupo" to { InviteToGroupPreview() },
        "invitar_plan" to { InvitePlanPreview() },
        "25_mi_codigo_escanear" to { MyCodeScanPreview() },
        "89_sin_conexion" to { OfflinePreview() },
        "89_sin_conexion_aviso" to { OfflineNoticePreview() },
        "mantenimiento" to { MaintenancePreview() },
        "actualizar" to { UpdateRequiredPreview() },
        "43_neg_registro" to { BusinessRegisterPreview() },
        "44_neg_ubic" to { BusinessLocationPreview() },
        "45_neg_oper" to { BusinessOperationPreview() },
        "46_neg_verif" to { BusinessVerifyPreview() },
        "46_neg_verif_faltantes" to { BusinessMissingPreview() },
        "47_neg_listo" to { BusinessDonePreview() },
        "68_post_visita" to { ReviewFormPreview() },
        "78_res_com" to { ReviewDetailPreview() },
        "69_publicado" to { ReviewPublishedPreview() },
        "77_res_pend" to { PendingReviewsPreview() },
        "77_res_pend_vacio" to { PendingReviewsEmptyPreview() },
        "79_mis_res" to { MyReviewsPreview() },
        "mis_res_confirmar_eliminar" to { MyReviewsConfirmPreview() },
        "80_res_lugar" to { PlaceReviewsPreview() },
        "70_crear_pub" to { CreateMomentPreview() },
        "71_camara" to { CameraPreview() },
        "72_postal" to { PostcardEditorPreview() },
        "73_album" to { AlbumPreview() },
        "74_rec_plan" to { AlbumByPlanPreview() },
        "75_postal_ver" to { PostcardDetailPreview() },
        "postal_ver_polaroid" to { PostcardDetailPolaroidPreview() },
        "postal_ver_pasaporte" to { PostcardDetailPassportPreview() },
        "postal_no_disponible" to { PostcardGonePreview() },
        "76_rec_vacio" to { AlbumEmptyPreview() },
        "86_ayuda" to { HelpPreview() },
        "86_ayuda_sin_resultados" to { HelpNoResultsPreview() },
        "86_ayuda_reportar" to { HelpReportPreview() },
        "ia_palabra" to { PlanWordPreview() },
        "17_ajusta_plan" to { PlanAdjustPreview() },
        "generando_1" to { PlanGeneratingPreview() },
        "generando_3" to { PlanGeneratingStep3Preview() },
        "generando_4" to { PlanReadyPreview() },
        "39_plan" to { PlanDetailPreview() },
        "plan_paseo_familiar" to { PlanFamilyPreview() },
        "40_cambiar" to { PlanChangePreview() },
        "41_agregar_parada" to { PlanAddStopPreview() },
        "crear_plan" to { ManualPlanPreview() },
        "26_checkin_ok" to { ArrivalOkPreview() },
        "llegada_sin_permiso" to { ArrivalPermissionPreview() },
        "28_vaca" to { VacaPreview() },
        "vaca_mariana_pago" to { VacaOnePaidPreview() },
        "vaca_todos_pagaron" to { VacaAllPaidPreview() },
        "vaca_vacia" to { VacaEmptyPreview() },
        "45_agregar_gasto" to { AddExpensePreview() },
        "45_agregar_gasto_personalizado" to { AddExpenseCustomPreview() },
        "48_neg_panel" to { BusinessPanelPreview() },
        "49_neg_reservas" to { BusinessReservationsPreview() },
        "49_neg_reservas_vacio" to { BusinessReservationsEmptyPreview() },
        "50_neg_checkin" to { CheckinPreview() },
        "50_neg_checkin_vacio" to { CheckinEmptyPreview() },
        "56_neg_stats" to { BusinessStatsPreview() },
        "53_neg_cupones" to { CouponsPreview() },
        "53_neg_cupones_vacio" to { CouponsEmptyPreview() },
        "54_neg_crear" to { CouponFormPreview() },
        "55_neg_horas" to { CapacityPreview() },
        "51_neg_resenas" to { BusinessReviewsPreview() },
        "52_neg_resp" to { ReviewReplyPreview() },
        "neg_momentos" to { BusinessMomentsPreview() }
    )

    @Test
    fun claro() = shootAll("claro")

    @Test
    @Config(qualifiers = "+night")
    fun oscuro() = shootAll("oscuro")

    @Test
    @Config(qualifiers = SMALL_PHONE, fontScale = LARGE_FONT)
    fun chica_letra_grande() = shootAll("chica_letra_grande")

    /** Bug nv-06: en horizontal la ilustración tapaba el texto de Bienvenida. */
    @Test
    @Config(qualifiers = "+land")
    fun bienvenida_horizontal() {
        compose.setContent { WelcomePreview() }
        compose.onNodeWithTag("welcome_art").assertDoesNotExist()
        compose.onNodeWithTag("welcome_create_account").assertExists()
        compose.onRoot().captureRoboImage(
            "src/test/screenshots/01_bienvenida_horizontal.png",
            roborazziOptions = OPTIONS
        )
    }

    private fun shootAll(variant: String) {
        var current: @Composable () -> Unit by mutableStateOf({})
        compose.setContent { current() }
        screens.forEach { (name, screen) ->
            current = screen
            compose.waitForIdle()
            compose.onRoot().captureRoboImage("src/test/screenshots/${name}_$variant.png", roborazziOptions = OPTIONS)
        }
    }

    private companion object {
        // Tolerancia para el suavizado del texto entre sistemas (Windows en local, Linux en CI).
        val OPTIONS = RoborazziOptions(compareOptions = RoborazziOptions.CompareOptions(changeThreshold = 0.01f))
    }
}
