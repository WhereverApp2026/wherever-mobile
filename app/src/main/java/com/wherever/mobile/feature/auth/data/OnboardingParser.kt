package com.wherever.mobile.feature.auth.data

import com.wherever.mobile.feature.auth.domain.FriendSuggestion
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull

object OnboardingParser {

    fun onboardingComplete(body: JsonElement): Boolean? {
        val candidates = buildList {
            add(body.objectOrNull())
            val root = body.objectOrNull()
            add(root?.get("data")?.objectOrNull())
            add(root?.get("usuario")?.objectOrNull())
            add(root?.get("data")?.objectOrNull()?.get("usuario")?.objectOrNull())
        }
        return candidates.filterNotNull()
            .firstNotNullOfOrNull { candidate ->
                sequenceOf("onboarding_completo", "onboardingComplete", "completado")
                    .mapNotNull { key -> (candidate[key] as? JsonPrimitive)?.booleanOrNull }
                    .firstOrNull()
            }
    }

    fun suggestions(body: JsonElement): List<FriendSuggestion> {
        val array = body.arrayOrNull()
            ?: body.objectOrNull()?.firstArray("items", "sugerencias", "usuarios", "data")
            ?: body.objectOrNull()?.get("data")?.objectOrNull()?.firstArray("items", "sugerencias", "usuarios")
            ?: return emptyList()
        return array.mapNotNull(::suggestion)
    }

    private fun suggestion(element: JsonElement): FriendSuggestion? {
        val objectValue = element.objectOrNull() ?: return null
        val id = objectValue.firstString("usuario_id", "id", "userId", "_id") ?: return null
        val name = objectValue.firstString("nombre", "name", "usuario", "username").orEmpty()
        val detail = objectValue.firstString("bio", "ciudad", "detalle", "description")
        val mutual = objectValue.firstInt("amigos_en_comun", "mutual_friends", "amigosComun")
        return FriendSuggestion(
            id = id,
            name = name.ifBlank { id },
            detail = detail,
            mutualFriends = mutual
        )
    }

    private fun JsonObject.firstArray(vararg keys: String): JsonArray? = keys.firstNotNullOfOrNull { key -> (this[key] as? JsonArray) }

    private fun JsonObject.firstString(vararg keys: String): String? =
        keys.firstNotNullOfOrNull { key -> (this[key] as? JsonPrimitive)?.contentOrNull?.takeIf { it.isNotBlank() } }

    private fun JsonObject.firstInt(vararg keys: String): Int? =
        keys.firstNotNullOfOrNull { key -> (this[key] as? JsonPrimitive)?.intOrNull }

    private fun JsonElement.objectOrNull(): JsonObject? = this as? JsonObject

    private fun JsonElement.arrayOrNull(): JsonArray? = this as? JsonArray
}
