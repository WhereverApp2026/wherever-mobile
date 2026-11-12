package com.wherever.mobile.core.database

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/** Migraciones de Room contra los esquemas exportados en `app/schemas` (sin borrar la caché). */
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        WhereverDatabase::class.java
    )

    @Test
    fun migration1To2KeepsProfileWithoutSensitiveConsent() {
        helper.createDatabase(DB_NAME, 1).use { db ->
            db.execSQL(
                """
                INSERT INTO user_profile (id, name, email, emailVerified, mode, points, level, weekStreak, version,
                    tastes_categories, tastes_budgetMin, tastes_budgetMax, tastes_restrictions, tastes_allergies,
                    pref_mobility, pref_theme, pref_units, pref_notifReservations, pref_notifCoupons,
                    pref_notifGroups, pref_notifReviews, pref_notifPromotions,
                    priv_location, priv_personalizedLearning, priv_mood, priv_usageData, priv_searchableByPhone)
                VALUES ('u1', 'Ana', 'ana@ejemplo.com', 1, 'explorar', 50, 'Explorador', 0, 3,
                    'cafes', 100, 500, '', '', 'a_pie', 'sistema', 'km', 1, 1, 1, 1, 0, 1, 1, 0, 1, 0)
                """.trimIndent()
            )
        }

        helper.runMigrationsAndValidate(DB_NAME, 2, true, MIGRATION_1_2).use { db ->
            db.query("SELECT name, version, priv_sensitiveData FROM user_profile WHERE id = 'u1'").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals("Ana", cursor.getString(0))
                assertEquals(3L, cursor.getLong(1))
                assertEquals(0, cursor.getInt(2))
            }
        }
    }

    private companion object {
        const val DB_NAME = "migration-test.db"
    }
}
