package com.wherever.mobile.feature.auth.data

import android.content.Context
import androidx.core.content.edit
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/** Solo una marca por id de usuario: sin tokens ni datos personales. */
class SharedOnboardingPreferences @Inject constructor(@ApplicationContext context: Context) : OnboardingPreferences {
    private val prefs = context.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    override fun isTastesSkipped(userId: String): Boolean = prefs.getBoolean(userId, false)

    override fun markTastesSkipped(userId: String) {
        prefs.edit { putBoolean(userId, true) }
    }

    override fun clear() {
        prefs.edit { clear() }
    }
}
