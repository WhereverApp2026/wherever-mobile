package com.wherever.mobile.core.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext

/**
 * Constitución §6 y MASVS-PLATFORM-3: mientras esta pantalla está a la vista, Android no la deja capturar,
 * grabar ni mostrar en la vista de apps recientes (`FLAG_SECURE`). Se usa en pantallas con contraseñas,
 * el código de amigo o datos personales. Al salir se quita para no bloquear capturas en el resto de la app.
 */
@Composable
fun SecureScreen() {
    val activity = LocalContext.current.findActivity() ?: return
    DisposableEffect(activity) {
        activity.window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        onDispose { activity.window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE) }
    }
}

internal tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
