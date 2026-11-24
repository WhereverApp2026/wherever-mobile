package com.wherever.mobile.feature.status.ui

import androidx.lifecycle.ViewModel
import com.wherever.mobile.core.security.AppLockPolicy
import com.wherever.mobile.core.security.AppLockPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Clock
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Bloqueo con huella o PIN al abrir la app y al volver de segundo plano (constitución E-05). */
@HiltViewModel
class AppLockViewModel @Inject constructor(private val preferences: AppLockPreferences, private val clock: Clock) :
    ViewModel() {
    private val mutableLocked = MutableStateFlow(preferences.enabled.value)
    val locked: StateFlow<Boolean> = mutableLocked.asStateFlow()
    private var backgroundSince: Long? = null

    fun onBackground() {
        backgroundSince = clock.millis()
    }

    fun onForeground() {
        if (AppLockPolicy.lockOnReturn(preferences.enabled.value, backgroundSince, clock.millis())) {
            mutableLocked.value = true
        }
        backgroundSince = null
    }

    fun unlocked() {
        mutableLocked.value = false
    }
}
