package com.wherever.mobile.security

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.wherever.mobile.core.security.EncryptedTokenStore
import com.wherever.mobile.core.security.Tokens
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EncryptedTokenStoreTest {

    private lateinit var context: Context
    private lateinit var store: EncryptedTokenStore

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        store = EncryptedTokenStore(context)
        store.reset()
    }

    @After
    fun tearDown() {
        store.reset()
    }

    @Test
    fun savesAndReadsTokens() {
        store.save(Tokens(accessToken = "access-1", refreshToken = "refresh-1"))

        assertEquals("access-1", store.accessToken())
        assertEquals("refresh-1", store.refreshToken())
    }

    @Test
    fun clearsTokens() {
        store.save(Tokens(accessToken = "access-1", refreshToken = "refresh-1"))

        store.clear()

        assertNull(store.accessToken())
        assertNull(store.refreshToken())
    }

    @Test
    fun doesNotPersistPlaintext() {
        store.save(Tokens(accessToken = "access-secret", refreshToken = "refresh-secret"))

        val storedValues = context
            .getSharedPreferences("wherever_secure_tokens", Context.MODE_PRIVATE)
            .all
            .values
            .joinToString(" ")

        assertFalse(storedValues.contains("access-secret"))
        assertFalse(storedValues.contains("refresh-secret"))
    }
}
