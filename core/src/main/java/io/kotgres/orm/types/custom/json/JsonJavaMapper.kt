package io.kotgres.orm.types.custom.json

import io.kotgres.orm.types.base.CustomMapper
import org.json.JSONObject

/**
 * Enum in DB and code
 */
class JsonJavaMapper : CustomMapper<JSONObject>(JSONObject::class) {

    override val postgresTypes: List<String>?
        get() = listOf("json", "jsonb")

    override fun fromSql(string: String): JSONObject? {
        return JSONObject(string)
    }

    override fun toSql(value: JSONObject): String {
        return value.toString()
    }

}
