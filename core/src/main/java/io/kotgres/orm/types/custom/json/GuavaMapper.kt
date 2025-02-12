package io.kotgres.orm.types.custom.json

import com.google.gson.JsonElement
import com.google.gson.JsonParser
import io.kotgres.orm.types.base.CustomMapper

/**
 * Enum in DB and code
 */
class GuavaMapper : CustomMapper<JsonElement>(JsonElement::class) {

    override val postgresTypes: List<String>?
        get() = listOf("json", "jsonb")

    override fun fromSql(string: String): JsonElement? {
        return JsonParser.parseString(string)
    }

    override fun toSql(value: JsonElement): String {
        return value.toString()
    }

}
