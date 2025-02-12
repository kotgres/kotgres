package io.kotgres.orm.internal.processors.model

internal data class EntityInfo(
    val fieldsInfo: List<PropertyInfo>,
    val primaryKeyInfo: PropertyInfo?,
)
