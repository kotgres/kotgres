package io.kotgres.types.json.jackson

import com.fasterxml.jackson.databind.JsonNode
import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.json.JsonConstants.TABLE_WITH_JSON_BINARY


@Table(name = TABLE_WITH_JSON_BINARY)
class JsonBinaryAsJackson(
    @PrimaryKey
    @Generated
    val id: Int,
    val content: JsonNode,
)
