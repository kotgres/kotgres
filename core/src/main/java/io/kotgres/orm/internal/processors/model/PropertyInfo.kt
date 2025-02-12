package io.kotgres.orm.internal.processors.model

import com.google.devtools.ksp.symbol.KSType

internal data class PropertyInfo(
    val columnName: String,
    val fieldName: String,
    val type: KSType,
    val isGenerated: Boolean,
    val mapperInfo: MapperInfo,
    val postgresType: String,
    val isPrimaryKey: Boolean,
    val allowUpdates: Boolean,
    val isUnique: Boolean,
    val hasEnumAnnotation: Boolean,
    val isEnumType: Boolean,
    val isNullable: Boolean,
)
