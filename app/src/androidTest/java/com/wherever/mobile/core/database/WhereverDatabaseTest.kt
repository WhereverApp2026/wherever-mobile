package com.wherever.mobile.core.database

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WhereverDatabaseTest {

    private lateinit var context: Context
    private lateinit var database: WhereverDatabase

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, WhereverDatabase::class.java)
            .allowMainThreadQueries()
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun opensDatabase() {
        database.openHelper.writableDatabase
        assertTrue(database.isOpen)
    }

    @Test
    fun writesAndReadsMetadata() = runBlocking {
        database.appMetadataDao().upsert(AppMetadataEntity(key = "schema", value = "ready"))

        assertEquals("ready", database.appMetadataDao().value("schema"))
    }

    @Test
    fun exportsVersionOneSchema() {
        val schema = InstrumentationRegistry.getInstrumentation().context.assets
            .open("com.wherever.mobile.core.database.WhereverDatabase/1.json")
            .bufferedReader()
            .use { it.readText() }

        assertTrue(schema.contains("\"version\": 1"))
        assertTrue(schema.contains("app_metadata"))
    }

    @Test
    fun exportsVersionTwoSchemaWithOnboardingState() {
        val schema = InstrumentationRegistry.getInstrumentation().context.assets
            .open("com.wherever.mobile.core.database.WhereverDatabase/2.json")
            .bufferedReader()
            .use { it.readText() }

        assertTrue(schema.contains("\"version\": 2"))
        assertTrue(schema.contains("onboarding_state"))
    }

    @Test
    fun writesAndReadsOnboardingState() = runBlocking {
        database.onboardingDao().upsert(
            OnboardingStateEntity(
                step = "PREFERENCES",
                interests = "cafe,musica",
                vibe = "social",
                budget = "medio",
                company = "amigos",
                updatedAt = 1L
            )
        )

        val stored = database.onboardingDao().get()

        assertEquals("PREFERENCES", stored?.step)
        assertEquals("cafe,musica", stored?.interests)

        database.onboardingDao().clear()
        assertEquals(null, database.onboardingDao().get())
    }
}
