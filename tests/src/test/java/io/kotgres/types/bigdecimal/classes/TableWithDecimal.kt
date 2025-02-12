package io.kotgres.types.bigdecimal.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import java.math.BigDecimal

@Table(name = "table_with_decimal")
class TableWithDecimal(
    @PrimaryKey
    @Generated
    val id: Int,
    val decimal: BigDecimal,
)
