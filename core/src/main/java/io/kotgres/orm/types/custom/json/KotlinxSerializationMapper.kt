package io.kotgres.orm.types.custom.json

import io.kotgres.orm.types.base.CustomMapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject

/**
 * Enum in DB and code
 */
class KotlinxSerializationMapper : CustomMapper<JsonElement>(JsonElement::class) {

    override val postgresTypes: List<String>?
        get() = listOf("json", "jsonb")

    override fun fromSql(string: String): JsonElement? {
        return Json.decodeFromString(string)
    }

    override fun toSql(value: JsonElement): String {
        return Json.encodeToString(value)
    }

}
