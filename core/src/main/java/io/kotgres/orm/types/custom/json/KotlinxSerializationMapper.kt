package io.kotgres.orm.types.custom.json

import io.kotgres.orm.types.base.CustomMapper
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

/**
 * Enum in DB and code
 */
class KotlinxSerializationMapper : CustomMapper<JsonObject>(JsonObject::class) {

    override val postgresTypes: List<String>?
        get() = listOf("json", "jsonb")

    override fun fromSql(string: String): JsonObject? {
        return Json.decodeFromString(string)
    }

    override fun toSql(value: JsonObject): String {
        return Json.encodeToString(value)
    }

}
