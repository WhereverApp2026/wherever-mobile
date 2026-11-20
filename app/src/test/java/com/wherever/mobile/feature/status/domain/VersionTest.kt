package com.wherever.mobile.feature.status.domain

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class VersionTest {
    @Test
    fun `compara por partes numericas`() {
        assertTrue(isOlder("0.7.0", "0.8.0"))
        assertTrue(isOlder("0.9.9", "0.10.0"))
        assertTrue(isOlder("1.0", "1.0.1"))
        assertFalse(isOlder("0.7.0", "0.0.0"))
        assertFalse(isOlder("1.0.0", "1.0"))
        assertFalse(isOlder("0.7.0-debug", "0.7.0"))
    }
}
