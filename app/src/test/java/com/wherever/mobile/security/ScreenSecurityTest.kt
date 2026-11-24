package com.wherever.mobile.security

import java.io.File
import org.junit.Assert.assertTrue
import org.junit.Test

/** MASVS-PLATFORM-3: las pantallas con contraseñas, código de amigo o datos personales usan `FLAG_SECURE`. */
class ScreenSecurityTest {
    private val sensitive = listOf(
        "feature/auth/ui/LoginScreen.kt",
        "feature/auth/ui/RegisterScreen.kt",
        "feature/auth/ui/NewPasswordScreen.kt",
        "feature/account/ui/ChangePasswordScreen.kt",
        "feature/account/ui/ChangeEmailScreen.kt",
        "feature/account/ui/MyDataScreen.kt",
        "feature/friends/ui/MyCodeScreen.kt",
        "feature/profile/ui/EditProfileScreen.kt",
        "feature/settings/ui/DeleteAccountScreen.kt",
        "feature/settings/ui/SessionsScreen.kt"
    )

    @Test
    fun `las pantallas sensibles no se pueden capturar`() {
        val missing = sensitive.filterNot {
            File("src/main/java/com/wherever/mobile/$it").readText().contains("SecureScreen()")
        }
        assertTrue("Sin FLAG_SECURE: $missing", missing.isEmpty())
    }
}
