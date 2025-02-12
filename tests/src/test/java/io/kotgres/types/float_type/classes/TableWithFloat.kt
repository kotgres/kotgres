package io.kotgres.types.float_type.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table

@Table(name = "table_with_float")
class TableWithFloat(
    @PrimaryKey
    @Generated
    val id: Int,
    val float: Float,
)
