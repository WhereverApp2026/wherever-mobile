package com.wherever.mobile.core.database

import androidx.room.TypeConverter
import kotlinx.serialization.json.Json

class StringListConverter {
    @TypeConverter
    fun fromList(value: List<String>): String = Json.encodeToString(value)

    @TypeConverter
    fun toList(value: String): List<String> = Json.decodeFromString(value)
}
