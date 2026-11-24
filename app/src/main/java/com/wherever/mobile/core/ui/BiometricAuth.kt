package com.wherever.mobile.core.ui

import android.content.Context
import android.os.Build
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

/**
 * Huella o rostro fuertes, o el PIN del teléfono como respaldo (constitución E-05). Android 9 y 10 no admiten
 * «fuerte + PIN» juntos, así que ahí se acepta la biometría débil con PIN.
 */
private val AUTHENTICATORS: Int
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
    } else {
        BIOMETRIC_WEAK or DEVICE_CREDENTIAL
    }

/** `true` si el teléfono tiene huella, rostro o un bloqueo de pantalla configurado. */
fun canUseAppLock(context: Context): Boolean =
    BiometricManager.from(context).canAuthenticate(AUTHENTICATORS) == BiometricManager.BIOMETRIC_SUCCESS

/** Muestra el diálogo del sistema. [onResult] recibe `true` solo si la persona se identificó. */
fun authenticate(context: Context, title: String, subtitle: String, onResult: (Boolean) -> Unit) {
    val activity = context.findActivity() as? FragmentActivity ?: return onResult(false)
    val prompt = BiometricPrompt(
        activity,
        ContextCompat.getMainExecutor(activity),
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) = onResult(true)

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = onResult(false)
        }
    )
    prompt.authenticate(
        BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(AUTHENTICATORS)
            .build()
    )
}
