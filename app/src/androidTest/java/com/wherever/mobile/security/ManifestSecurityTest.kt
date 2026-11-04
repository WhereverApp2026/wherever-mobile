package com.wherever.mobile.security

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.security.NetworkSecurityPolicy
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.MainActivity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ManifestSecurityTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private val packageManager = context.packageManager
    private val packageName = context.packageName

    @Test
    fun backupIsDisabled() {
        val applicationInfo = packageManager.getApplicationInfo(
            packageName,
            PackageManager.GET_META_DATA
        )

        assertEquals(0, applicationInfo.flags and ApplicationInfo.FLAG_ALLOW_BACKUP)
    }

    @Test
    fun cleartextTrafficIsDisabled() {
        assertFalse(NetworkSecurityPolicy.getInstance().isCleartextTrafficPermitted)
    }

    @Test
    fun onlyLauncherActivityIsExported() {
        val activities = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_ACTIVITIES
        ).activities.orEmpty()

        val exported = activities
            .filter { it.name.startsWith("$packageName.") && it.exported }
            .map { it.name }

        assertEquals(listOf(MainActivity::class.java.name), exported)
    }

    @Test
    fun servicesAndReceiversAreNotExported() {
        val info = packageManager.getPackageInfo(
            packageName,
            PackageManager.GET_SERVICES or PackageManager.GET_RECEIVERS
        )

        assertTrue(info.services.orEmpty().none { it.name.startsWith("$packageName.") && it.exported })
        assertTrue(info.receivers.orEmpty().none { it.name.startsWith("$packageName.") && it.exported })
    }
}
