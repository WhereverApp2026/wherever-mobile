package com.wherever.mobile.feature.auth.domain

/** La API no indica si se omitieron los gustos (duda D12): se recuerda en el dispositivo. */
interface OnboardingPreferences {
    fun isTastesSkipped(userId: String): Boolean

    fun markTastesSkipped(userId: String)

    fun clear()
}
