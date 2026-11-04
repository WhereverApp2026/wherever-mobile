package com.wherever.mobile.core.designsystem.components

sealed interface WhereverContentState {
    data object Loading : WhereverContentState
    data object Empty : WhereverContentState
    data object Offline : WhereverContentState
    data class Error(val message: String? = null) : WhereverContentState
}
