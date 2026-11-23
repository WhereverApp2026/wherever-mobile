package com.wherever.mobile.feature.friends.domain

import com.wherever.mobile.core.common.Result

/** La API acepta de 1 a 500 teléfonos por llamada (`POST /personas/contactos`, backend nv-70). */
const val MAX_CONTACT_PHONES = 500

/** Alguien de mis contactos que ya usa Wherever, con nuestra relación actual. */
data class ContactMatch(val person: Person, val relation: Relation)

/** Lee los teléfonos de la agenda del teléfono. Solo se llama con `READ_CONTACTS` concedido. */
interface ContactsReader {
    suspend fun phoneNumbers(): List<String>
}

/**
 * 07_EncuentraAmigos (US-233). Los teléfonos viajan por HTTPS; el servidor los busca por su HMAC en memoria
 * y nunca los guarda ni los registra (contrato de `POST /personas/contactos`). La app tampoco los guarda.
 */
interface ContactMatchRepository {
    suspend fun match(phones: List<String>): Result<List<ContactMatch>>
}
