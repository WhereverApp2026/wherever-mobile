package com.wherever.mobile.feature.tastes.ui

import com.wherever.mobile.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TasteLabelTest {
    @Test
    fun knownKeyUsesCatalogLabelNotTheKey() {
        assertEquals(R.string.taste_cafes, tasteLabelRes("cafes"))
    }

    @Test
    fun unknownKeyHasNoLabel() {
        assertNull(tasteLabelRes("clave_nueva"))
    }
}
