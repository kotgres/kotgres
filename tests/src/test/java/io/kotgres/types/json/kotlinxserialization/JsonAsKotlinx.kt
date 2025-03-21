package io.kotgres.types.json.kotlinxserialization

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.annotations.UseCustomMapper
import io.kotgres.orm.types.custom.json.KotlinxSerializationMapper
import io.kotgres.types.json.JsonConstants.TABLE_WITH_JSON
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject


@Table(name = TABLE_WITH_JSON)
class JsonAsKotlinx(
    @PrimaryKey
    @Generated
    val id: Int,
    @UseCustomMapper(customMapper = KotlinxSerializationMapper::class)
    val content: JsonElement,
)
