package com.wherever.mobile.feature.friends.data

import android.content.Context
import android.provider.ContactsContract
import com.wherever.mobile.core.common.DispatcherProvider
import com.wherever.mobile.core.common.Result
import com.wherever.mobile.core.network.safeApiCall
import com.wherever.mobile.feature.friends.domain.ContactMatch
import com.wherever.mobile.feature.friends.domain.ContactMatchRepository
import com.wherever.mobile.feature.friends.domain.ContactsReader
import com.wherever.mobile.feature.friends.domain.MAX_CONTACT_PHONES
import com.wherever.mobile.feature.friends.domain.Relation
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.POST

@Serializable
data class ContactPhonesDto(@SerialName("telefonos") val phones: List<String>)

@Serializable
data class ContactMatchDto(
    @SerialName("indice") val index: Int = 0,
    @SerialName("persona") val person: PersonDto,
    @SerialName("relacion") val relation: String = "ninguna"
)

@Serializable
data class ContactMatchListDto(@SerialName("resultados") val results: List<ContactMatchDto> = emptyList())

/** wherever-backend `docs/endpoints.md`: `POST /api/v1/personas/contactos` (10 por día por usuario). */
interface ContactsApi {
    @POST("personas/contactos")
    suspend fun match(@Body body: ContactPhonesDto): ContactMatchListDto
}

class ContactMatchRepositoryImpl @Inject constructor(private val api: ContactsApi) : ContactMatchRepository {
    override suspend fun match(phones: List<String>): Result<List<ContactMatch>> = safeApiCall {
        api.match(ContactPhonesDto(phones.distinct().take(MAX_CONTACT_PHONES))).results.map {
            ContactMatch(it.person.toDomain(), Relation.from(it.relation))
        }
    }
}

/** Agenda del teléfono vía `ContactsContract`: solo números, sin nombres; nada se guarda ni se registra. */
class AndroidContactsReader @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dispatchers: DispatcherProvider
) : ContactsReader {
    override suspend fun phoneNumbers(): List<String> = withContext(dispatchers.io) {
        val numbers = LinkedHashSet<String>()
        val cursor = context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            null,
            null,
            null
        ) ?: return@withContext emptyList()
        cursor.use {
            val column = it.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            while (it.moveToNext() && numbers.size < MAX_CONTACT_PHONES) {
                it.getString(column)?.filter { c -> c.isDigit() || c == '+' }?.takeIf { n -> n.length >= MIN_DIGITS }
                    ?.let(numbers::add)
            }
        }
        numbers.toList()
    }

    private companion object {
        /** Menos de 7 dígitos no es un teléfono (extensiones, códigos cortos). */
        const val MIN_DIGITS = 7
    }
}
