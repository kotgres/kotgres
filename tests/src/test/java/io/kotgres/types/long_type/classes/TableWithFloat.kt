package io.kotgres.types.long_type.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table

@Table(name = "table_with_bigint")
class TableWithBigInt(
    @PrimaryKey
    @Generated
    val id: Int,
    val long: Long,
)
