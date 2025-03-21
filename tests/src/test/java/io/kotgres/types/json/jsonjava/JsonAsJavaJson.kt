package io.kotgres.types.json.jsonjava

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.annotations.CustomMapper
import io.kotgres.orm.types.custom.json.JsonJavaMapper
import io.kotgres.types.json.JsonConstants.TABLE_WITH_JSON
import org.json.JSONObject


@Table(name = TABLE_WITH_JSON)
class JsonAsJavaJson(
    @PrimaryKey
    @Generated
    val id: Int,
    @CustomMapper(mapperClass = JsonJavaMapper::class)
    val content: JSONObject,
)
