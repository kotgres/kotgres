package io.kotgres.types.timestamp.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.timestamp.TEST_TIMESTAMP_TABLE
import java.util.Date

@Table(name = TEST_TIMESTAMP_TABLE)
data class TableWithTimestampAsDate(
    @PrimaryKey
    @Generated
    val id: Int,
    val date: Date,
)