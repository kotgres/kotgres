package io.kotgres.types.json.gson

import com.google.gson.JsonElement
import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.annotations.CustomMapper
import io.kotgres.orm.types.custom.json.GuavaMapper
import io.kotgres.types.json.JsonConstants.TABLE_WITH_JSON


@Table(name = TABLE_WITH_JSON)
class JsonAsGson(
    @PrimaryKey
    @Generated
    val id: Int,
    @CustomMapper(mapperClass = GuavaMapper::class)
    val content: JsonElement,
)
