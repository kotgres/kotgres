package io.kotgres.orm.dao.model

import io.kotgres.orm.types.base.AbstractMapper

data class ValueWithMapper(
    val value: Any?,
    val mapper: AbstractMapper<*>,
)
