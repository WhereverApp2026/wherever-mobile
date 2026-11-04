package com.wherever.mobile.core.common

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class PageResultTest {

    @Test
    fun usesContractDefaults() {
        val page = PageResult<String>()

        assertEquals(emptyList<String>(), page.items)
        assertNull(page.nextCursor)
        assertEquals(20, page.limit)
        assertFalse(page.hasMore())
    }

    @Test
    fun rejectsLimitsOutsideContractRange() {
        assertThrows(IllegalArgumentException::class.java) { PageResult<String>(limit = 0) }
        assertThrows(IllegalArgumentException::class.java) { PageResult<String>(limit = 51) }
    }

    @Test
    fun serializesContractFieldNames() {
        val page = PageResult(items = listOf("a"), nextCursor = "cursor-1", limit = 10)

        val json = Json.encodeToString(PageResult.serializer(String.serializer()), page)

        assertTrue(json.contains("\"siguienteCursor\":\"cursor-1\""))
        assertTrue(json.contains("\"limite\":10"))
        assertFalse(json.contains("nextCursor"))
    }

    @Test
    fun mapsItemsAndKeepsPagination() {
        val page = PageResult(items = listOf(1, 2), nextCursor = "next", limit = 5)

        val mapped = page.map { it * 10 }

        assertEquals(listOf(10, 20), mapped.items)
        assertEquals("next", mapped.nextCursor)
        assertEquals(5, mapped.limit)
        assertTrue(mapped.hasMore())
    }
}
