package com.wherever.mobile.core.designsystem

import android.provider.Settings
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ContentTransform
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset

/** Duraciones y curvas de la app. Ningún otro archivo fija tiempos de animación (specs/nv-45). */
object WhereverMotion {
    /** Cambio de pantalla. */
    const val NAV_MS = 300

    /** Cambio entre cargando, error y contenido dentro de una pantalla. */
    const val CONTENT_MS = 220

    /** Errores de campo y cambio del botón a "cargando". */
    const val SHORT_MS = 150

    /** Escala inicial de las pantallas tipo resultado (Revisa tu correo, Eliminación pendiente). */
    const val RESULT_SCALE = 0.92f

    /** Escala inicial del indicador de carga de un botón. */
    const val LOADING_SCALE = 0.8f

    /** La pantalla que se va se corre solo una cuarta parte, para que la nueva quede encima. */
    const val PARALLAX_DIVISOR = 4

    /** Material 3 "emphasized decelerate": entra rápido y frena suave. */
    val Enter: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)

    /** Material 3 "emphasized accelerate": para lo que sale. */
    val Exit: Easing = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)

    val Standard: Easing = FastOutSlowInEasing
}

/** `false` si el usuario apagó las animaciones del sistema (escala del animador en 0). */
val LocalMotionEnabled = staticCompositionLocalOf { true }

/** Regla pura para poder probarla en la JVM. */
fun motionEnabled(animatorDurationScale: Float): Boolean = animatorDurationScale > 0f

@Composable
internal fun rememberSystemMotionEnabled(): Boolean {
    val resolver = LocalContext.current.contentResolver
    return remember(resolver) {
        motionEnabled(Settings.Global.getFloat(resolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f))
    }
}

/** Transiciones entre pantallas. Con las animaciones apagadas, el cambio es inmediato. */
object NavMotion {
    private fun <T> navTween(easing: Easing): FiniteAnimationSpec<T> = tween(WhereverMotion.NAV_MS, easing = easing)

    private fun offsetTween(easing: Easing): FiniteAnimationSpec<IntOffset> =
        tween(WhereverMotion.NAV_MS, easing = easing)

    /** Avanzar: la nueva entra desde la derecha. */
    fun forwardEnter(enabled: Boolean): EnterTransition = if (!enabled) {
        EnterTransition.None
    } else {
        slideInHorizontally(offsetTween(WhereverMotion.Enter)) { it } + fadeIn(navTween(WhereverMotion.Enter))
    }

    /** Avanzar: la de atrás se corre un poco a la izquierda. */
    fun forwardExit(enabled: Boolean): ExitTransition = if (!enabled) {
        ExitTransition.None
    } else {
        slideOutHorizontally(offsetTween(WhereverMotion.Exit)) { -it / WhereverMotion.PARALLAX_DIVISOR } +
            fadeOut(navTween(WhereverMotion.Exit))
    }

    /** Regresar: al revés que avanzar. */
    fun backEnter(enabled: Boolean): EnterTransition = if (!enabled) {
        EnterTransition.None
    } else {
        slideInHorizontally(offsetTween(WhereverMotion.Enter)) { -it / WhereverMotion.PARALLAX_DIVISOR } +
            fadeIn(navTween(WhereverMotion.Enter))
    }

    fun backExit(enabled: Boolean): ExitTransition = if (!enabled) {
        ExitTransition.None
    } else {
        slideOutHorizontally(offsetTween(WhereverMotion.Exit)) { it } + fadeOut(navTween(WhereverMotion.Exit))
    }

    /** Pantallas tipo resultado: aparecen con fundido y una escala ligera. */
    fun resultEnter(enabled: Boolean): EnterTransition = if (!enabled) {
        EnterTransition.None
    } else {
        fadeIn(navTween(WhereverMotion.Enter)) +
            scaleIn(navTween(WhereverMotion.Enter), initialScale = WhereverMotion.RESULT_SCALE)
    }

    fun resultExit(enabled: Boolean): ExitTransition = if (!enabled) {
        ExitTransition.None
    } else {
        fadeOut(navTween(WhereverMotion.Exit)) +
            scaleOut(navTween(WhereverMotion.Exit), targetScale = WhereverMotion.RESULT_SCALE)
    }
}

/** Fases de una pantalla que carga datos. */
enum class ScreenPhase { LOADING, ERROR, CONTENT }

/** Sin animación: `togetherWith` trae un resorte de tamaño que dejaría el contenido anterior a la vista. */
internal fun noMotion(): ContentTransform =
    ContentTransform(EnterTransition.None, ExitTransition.None, sizeTransform = null)

/** Solo fundido: sin `SizeTransform`, la pantalla no se estira ni se recorta al cambiar de fase. */
internal fun crossfade(enabled: Boolean, durationMs: Int): ContentTransform = if (enabled) {
    ContentTransform(
        targetContentEnter = fadeIn(tween(durationMs, easing = WhereverMotion.Standard)),
        initialContentExit = fadeOut(tween(durationMs, easing = WhereverMotion.Standard)),
        sizeTransform = null
    )
} else {
    noMotion()
}

/** Fundido entre cargando, error y contenido. Solo anima cuando cambia la fase, no con cada dato. */
@Composable
fun PhaseCrossfade(phase: ScreenPhase, modifier: Modifier = Modifier, content: @Composable (ScreenPhase) -> Unit) {
    val enabled = LocalMotionEnabled.current
    AnimatedContent(
        targetState = phase,
        modifier = modifier,
        transitionSpec = { crossfade(enabled, WhereverMotion.CONTENT_MS) },
        label = "phase"
    ) { content(it) }
}

/** Cambio corto entre el texto de un botón y su indicador de carga. */
@Composable
internal fun LoadingSwap(loading: Boolean, content: @Composable (Boolean) -> Unit) {
    val enabled = LocalMotionEnabled.current
    AnimatedContent(
        targetState = loading,
        transitionSpec = {
            if (enabled) {
                (
                    fadeIn(tween(WhereverMotion.SHORT_MS)) +
                        scaleIn(tween(WhereverMotion.SHORT_MS), initialScale = WhereverMotion.LOADING_SCALE)
                    )
                    .togetherWith(fadeOut(tween(WhereverMotion.SHORT_MS)))
            } else {
                noMotion()
            }
        },
        label = "loading"
    ) { content(it) }
}

/** Muestra u oculta un error de campo; conserva el último texto mientras se cierra. */
@Composable
fun AnimatedFieldError(visible: Boolean, content: @Composable () -> Unit) {
    val enabled = LocalMotionEnabled.current
    AnimatedVisibility(
        visible = visible,
        enter = if (enabled) {
            expandVertically(tween(WhereverMotion.SHORT_MS)) + fadeIn(tween(WhereverMotion.SHORT_MS))
        } else {
            EnterTransition.None
        },
        exit = if (enabled) {
            shrinkVertically(tween(WhereverMotion.SHORT_MS)) + fadeOut(tween(WhereverMotion.SHORT_MS))
        } else {
            ExitTransition.None
        }
    ) { content() }
}
