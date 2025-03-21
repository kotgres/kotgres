package io.kotgres.types.json.jackson

import com.fasterxml.jackson.databind.JsonNode
import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.annotations.UseCustomMapper
import io.kotgres.orm.types.custom.json.JacksonMapper
import io.kotgres.types.json.JsonConstants.TABLE_WITH_JSON


@Table(name = TABLE_WITH_JSON)
class JsonAsJackson(
    @PrimaryKey
    @Generated
    val id: Int,
    @UseCustomMapper(customMapper = JacksonMapper::class)
    val content: JsonNode,
)
