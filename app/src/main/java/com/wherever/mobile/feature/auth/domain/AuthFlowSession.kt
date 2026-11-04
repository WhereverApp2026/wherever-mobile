package com.wherever.mobile.feature.auth.domain

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthFlowSession @Inject constructor() {
    var recoveryEmail: String = ""
        private set

    fun setRecoveryEmail(email: String) {
        recoveryEmail = email
    }

    fun clear() {
        recoveryEmail = ""
    }
}
