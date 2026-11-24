package com.wherever.mobile.core.security

import android.content.Context
import androidx.core.content.edit
import com.wherever.mobile.core.common.SessionCleaner
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Desbloqueo con huella, rostro o el bloqueo del teléfono (constitución E-05): opcional, se activa en Ajustes. */
interface AppLockPreferences {
    val enabled: StateFlow<Boolean>

    fun setEnabled(enabled: Boolean)
}

/** Solo una marca: sin tokens ni datos personales. Se borra al cerrar sesión. */
@Singleton
class SharedAppLockPreferences @Inject constructor(@ApplicationContext context: Context) :
    AppLockPreferences,
    SessionCleaner {
    private val prefs = context.getSharedPreferences("app_lock", Context.MODE_PRIVATE)
    private val state = MutableStateFlow(prefs.getBoolean(KEY, false))
    override val enabled: StateFlow<Boolean> = state.asStateFlow()

    override fun setEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(KEY, enabled) }
        state.value = enabled
    }

    override suspend fun clear() = setEnabled(false)

    private companion object {
        const val KEY = "enabled"
    }
}

/** Cuándo pedir el desbloqueo: al abrir la app y al volver tras más de [GRACE_MILLIS] en segundo plano. */
object AppLockPolicy {
    const val GRACE_MILLIS = 60_000L

    fun lockOnReturn(enabled: Boolean, backgroundSinceMillis: Long?, nowMillis: Long): Boolean =
        enabled && backgroundSinceMillis != null && nowMillis - backgroundSinceMillis > GRACE_MILLIS
}
