package com.wherever.mobile.core.security

interface TokenCipher {
    fun encrypt(plain: ByteArray): ByteArray

    fun decrypt(encrypted: ByteArray): ByteArray
}
