package com.wherever.mobile.feature.auth.domain

data class DeviceInfo(val model: String, val system: String, val appVersion: String)

fun interface DeviceInfoProvider {
    fun deviceInfo(): DeviceInfo
}
