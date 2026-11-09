package com.wherever.mobile.feature.profile.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.core.database.WhereverDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/** Room real en memoria (constitución §2.7). Necesita emulador. */
@RunWith(AndroidJUnit4::class)
class UserProfileDaoTest {
    private lateinit var database: WhereverDatabase
    private lateinit var dao: UserProfileDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            InstrumentationRegistry.getInstrumentation().targetContext,
            WhereverDatabase::class.java
        ).build()
        dao = database.userProfileDao()
    }

    @After
    fun tearDown() = database.close()

    private fun profile(tastes: TastesDto? = TastesDto(listOf("cafes"), 100, 500, listOf("vegano"), "nueces")) =
        UserProfileDto(id = "u1", name = "Ana", email = "a@b.co", tastes = tastes, version = 3).toEntity()

    @Test
    fun guardaYLeeListasYObjetosAnidados() = runTest {
        dao.replace(profile())

        val stored = dao.observe().first()!!

        assertEquals(listOf("cafes"), stored.tastes?.categories)
        assertEquals(listOf("vegano"), stored.tastes?.restrictions)
        assertEquals(3L, stored.version)
    }

    @Test
    fun gustosNulosSiguenSiendoNulos() = runTest {
        dao.replace(profile(tastes = null))
        assertNull(dao.get()?.tastes)
    }

    @Test
    fun clearBorraElPerfil() = runTest {
        dao.replace(profile())
        dao.clear()
        assertNull(dao.get())
    }
}
