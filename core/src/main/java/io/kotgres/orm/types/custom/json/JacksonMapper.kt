package io.kotgres.orm.types.custom.json

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.kotgres.orm.types.base.CustomMapper


/**
 * Enum in DB and code
 */
class JacksonMapper : CustomMapper<JsonNode>(JsonNode::class) {

    private val objectMapper: ObjectMapper = ObjectMapper()

    override val postgresTypes: List<String>?
        get() = listOf("json", "jsonb")

    override fun fromSql(string: String): JsonNode? {
        return objectMapper.readTree(string)
    }

    override fun toSql(value: JsonNode): String {
        return value.toString()
    }

}
