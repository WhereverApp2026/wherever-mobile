package com.wherever.mobile.security

import com.wherever.mobile.feature.auth.data.AuthApi
import com.wherever.mobile.feature.auth.data.AuthRepositoryImpl
import com.wherever.mobile.feature.auth.domain.DeviceInfo
import com.wherever.mobile.feature.auth.domain.OnboardingPreferences
import com.wherever.mobile.feature.groups.data.GroupMessageEntity
import com.wherever.mobile.feature.groups.data.GroupMessagesCleaner
import com.wherever.mobile.testing.FakeDeviceRegistration
import com.wherever.mobile.testing.FakeGroupMessageDao
import com.wherever.mobile.testing.FakeProfileRepository
import com.wherever.mobile.testing.FakeTokenStore
import com.wherever.mobile.testing.FakeUserProfileDao
import com.wherever.mobile.testing.testRetrofit
import java.io.File
import kotlinx.coroutines.test.runTest
import mockwebserver3.MockWebServer
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * nv-25 (Security): los mensajes del chat son dato personal y no sobreviven a la sesión; el texto viaja
 * solo en el cuerpo (nunca en la URL) y el módulo registra su limpieza para el cierre de sesión.
 */
class GroupsSecurityTest {
    @Test
    fun `cerrar la sesion borra los mensajes guardados del chat`() = runTest {
        val server = MockWebServer().apply { start() }
        val messages = FakeGroupMessageDao()
        messages.upsert(
            listOf(
                GroupMessageEntity(
                    id = "m1",
                    groupId = "g1",
                    clientId = null,
                    authorId = "u2",
                    authorName = "Luis",
                    authorPhoto = null,
                    system = false,
                    text = "mi dirección",
                    deleted = false,
                    sentAt = "2026-11-20T18:00:00Z",
                    status = "SENT"
                )
            )
        )
        val repository = AuthRepositoryImpl(
            api = testRetrofit(server.url("/")).create(AuthApi::class.java),
            tokenStore = FakeTokenStore(),
            profileRepository = FakeProfileRepository(),
            profileDao = FakeUserProfileDao(),
            onboarding = NoOnboarding,
            deviceInfo = { DeviceInfo("Pixel 8", "Android 15", "0.1.0") },
            deviceRegistration = FakeDeviceRegistration(FakeTokenStore()),
            sessionCleaners = setOf(GroupMessagesCleaner(messages))
        )

        repository.clearLocalSession()

        assertTrue(messages.rows.value.isEmpty())
        server.close()
    }

    @Test
    fun `el modulo registra la limpieza y el texto no va en la URL`() {
        val module = File("src/main/java/com/wherever/mobile/feature/groups/data/di/GroupsModule.kt").readText()
        val api = File("src/main/java/com/wherever/mobile/feature/groups/data/GroupsApi.kt").readText()
        assertTrue(module.contains("@IntoSet") && module.contains("GroupMessagesCleaner"))
        assertFalse("el texto no puede ser @Query ni @Path", Regex("""@(Query|Path)\("texto"\)""").containsMatchIn(api))
        assertTrue(api.contains("@Body body: SendMessageDto"))
    }

    @Test
    fun `los mensajes no se registran en logs`() {
        listOf(
            "src/main/java/com/wherever/mobile/feature/groups/data/GroupsRepositoryImpl.kt",
            "src/main/java/com/wherever/mobile/feature/groups/ui/GroupChatViewModel.kt"
        ).forEach { path ->
            val source = File(path).readText()
            listOf("Log.", "println", "Timber").forEach { assertFalse("$path usa $it", source.contains(it)) }
        }
    }

    private object NoOnboarding : OnboardingPreferences {
        override fun isTastesSkipped(userId: String) = false

        override fun markTastesSkipped(userId: String) = Unit

        override fun clear() = Unit
    }
}
