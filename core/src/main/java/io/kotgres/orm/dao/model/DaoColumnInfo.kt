package io.kotgres.orm.dao.model

import io.kotgres.orm.types.base.AbstractMapper
import java.lang.reflect.Field

open class DaoColumnInfo<E, I>(
    val columnName: String,
    val getValue: (E) -> I?,
    val isGenerated: Boolean,
    val allowUpdates: Boolean,
    val declaredField: Field,
    val mapper: AbstractMapper<I>,
)
