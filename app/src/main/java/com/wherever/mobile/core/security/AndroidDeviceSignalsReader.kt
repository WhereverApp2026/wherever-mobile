package com.wherever.mobile.core.security

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Debug
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject

/**
 * Señales básicas de root, depurador y re-empaquetado. No reemplaza a Play Integrity (eso exige validar
 * el veredicto en el backend); sube el costo de manipular la app en un teléfono comprometido.
 */
class AndroidDeviceSignalsReader @Inject constructor(@param:ApplicationContext private val context: Context) :
    DeviceSignalsReader {
    override fun read(): DeviceSignals = DeviceSignals(
        suPresent = SU_PATHS.any { File(it).exists() },
        testKeys = Build.TAGS?.contains("test-keys") == true,
        debuggerAttached = Debug.isDebuggerConnected() || Debug.waitingForDebugger(),
        appDebuggable = context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE != 0,
        signingCertSha256 = signingCertSha256()
    )

    private fun signingCertSha256(): String? = try {
        val signer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val info = context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
            info.signingInfo?.apkContentsSigners?.firstOrNull()
        } else {
            @Suppress("DEPRECATION")
            context.packageManager.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
                .signatures?.firstOrNull()
        }
        signer?.toByteArray()?.let { bytes ->
            MessageDigest.getInstance("SHA-256").digest(bytes).joinToString("") { "%02x".format(it) }
        }
    } catch (_: PackageManager.NameNotFoundException) {
        null
    }

    private companion object {
        val SU_PATHS = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/data/local/su",
            "/data/local/bin/su",
            "/data/local/xbin/su",
            "/system/sd/xbin/su",
            "/system/app/Superuser.apk",
            "/data/adb/magisk"
        )
    }
}
