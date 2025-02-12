package io.kotgres.types.bool.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.bool.TYPE_BOOL_TEST_TABLE

@Table(name = TYPE_BOOL_TEST_TABLE)
class BoolAsBoolean(
    @PrimaryKey
    @Generated
    val id: Int,
    val bool: Boolean,
)
