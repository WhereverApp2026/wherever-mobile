package com.wherever.mobile.core.ui

import com.wherever.mobile.core.network.AppError

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data object Empty : UiState<Nothing>
    data object Offline : UiState<Nothing>
    data class Content<T>(val data: T) : UiState<T>
    data class Error(val error: AppError) : UiState<Nothing>
}
