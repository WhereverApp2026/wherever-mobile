package com.wherever.mobile.core.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PageResult<T>(
    val items: List<T> = emptyList(),
    @SerialName("siguienteCursor")
    val nextCursor: String? = null,
    @SerialName("limite")
    val limit: Int = DEFAULT_LIMIT
) {
    init {
        require(limit in 1..MAX_LIMIT) {
            "limit must be between 1 and $MAX_LIMIT"
        }
    }

    fun <R> map(transform: (T) -> R): PageResult<R> = PageResult(
        items = items.map(transform),
        nextCursor = nextCursor,
        limit = limit
    )

    fun hasMore(): Boolean = nextCursor != null

    companion object {
        const val DEFAULT_LIMIT = 20
        const val MAX_LIMIT = 50
    }
}
