package com.wherever.mobile.core.common

/** Página de una lista con cursor firmado (`siguiente_cursor`; null = no hay más). Constitución §4. */
data class PageResult<T>(val items: List<T>, val nextCursor: String?)
