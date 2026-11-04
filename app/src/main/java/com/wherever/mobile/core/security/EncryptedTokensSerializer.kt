package com.wherever.mobile.core.security

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import java.io.InputStream
import java.io.OutputStream
import java.security.GeneralSecurityException
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/** Cifra todo el archivo. Si no se puede descifrar, DataStore lo trata como corrupto. */
class EncryptedTokensSerializer(private val cipher: TokenCipher) : Serializer<SessionTokens?> {
    override val defaultValue: SessionTokens? = null

    override suspend fun readFrom(input: InputStream): SessionTokens? {
        val bytes = input.readBytes()
        if (bytes.isEmpty()) return null
        return try {
            Json.decodeFromString<SessionTokens>(cipher.decrypt(bytes).decodeToString())
        } catch (e: GeneralSecurityException) {
            throw CorruptionException("No se pudo descifrar la sesión", e)
        } catch (e: SerializationException) {
            throw CorruptionException("Sesión ilegible", e)
        } catch (e: IllegalArgumentException) {
            throw CorruptionException("Sesión ilegible", e)
        }
    }

    override suspend fun writeTo(t: SessionTokens?, output: OutputStream) {
        if (t != null) output.write(cipher.encrypt(Json.encodeToString(t).encodeToByteArray()))
    }
}
