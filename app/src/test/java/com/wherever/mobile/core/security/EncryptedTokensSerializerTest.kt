package com.wherever.mobile.core.security

import androidx.datastore.core.CorruptionException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import javax.crypto.AEADBadTagException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Test

/** Cifrado reversible de prueba: en el JVM no hay Android Keystore. */
private class XorCipher : TokenCipher {
    override fun encrypt(plain: ByteArray): ByteArray = plain.map { (it.toInt() xor 0x5A).toByte() }.toByteArray()

    override fun decrypt(encrypted: ByteArray): ByteArray = encrypt(encrypted)
}

private class BrokenCipher : TokenCipher {
    override fun encrypt(plain: ByteArray): ByteArray = plain

    override fun decrypt(encrypted: ByteArray): ByteArray = throw AEADBadTagException()
}

class EncryptedTokensSerializerTest {
    private val tokens = SessionTokens("acceso-abc", "refresh-def", "2026-11-04T15:15:00Z")

    private suspend fun write(serializer: EncryptedTokensSerializer, value: SessionTokens?): ByteArray {
        val out = ByteArrayOutputStream()
        serializer.writeTo(value, out)
        return out.toByteArray()
    }

    @Test
    fun `lo guardado no contiene los tokens en claro y se recupera igual`() = runTest {
        val serializer = EncryptedTokensSerializer(XorCipher())
        val bytes = write(serializer, tokens)

        assertFalse(bytes.decodeToString().contains("acceso-abc"))
        assertFalse(bytes.decodeToString().contains("refresh-def"))
        assertEquals(tokens, serializer.readFrom(ByteArrayInputStream(bytes)))
    }

    @Test
    fun `sin sesion se escribe vacio y se lee null`() = runTest {
        val serializer = EncryptedTokensSerializer(XorCipher())
        val bytes = write(serializer, null)

        assertEquals(0, bytes.size)
        assertNull(serializer.readFrom(ByteArrayInputStream(bytes)))
    }

    @Test(expected = CorruptionException::class)
    fun `si no se puede descifrar es corrupcion`() = runTest {
        EncryptedTokensSerializer(BrokenCipher()).readFrom(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
    }

    @Test(expected = CorruptionException::class)
    fun `contenido ilegible es corrupcion`() = runTest {
        EncryptedTokensSerializer(XorCipher()).readFrom(ByteArrayInputStream(byteArrayOf(1, 2, 3)))
    }
}
