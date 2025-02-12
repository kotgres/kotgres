package io.kotgres.types.real.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.real.TYPE_REAL_TEST_TABLE

@Table(name = TYPE_REAL_TEST_TABLE)
class RealAsInt(
    @PrimaryKey
    @Generated
    val id: Int,
    val real: Int,
)
