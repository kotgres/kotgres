package io.kotgres.types.timestamp.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.timestamp.TEST_TIMESTAMP_TABLE
import java.time.LocalDateTime

@Table(name = TEST_TIMESTAMP_TABLE)
data class TableWithTimestampAsLocalDateTime(
    @PrimaryKey
    @Generated
    val id: Int,
    val date: LocalDateTime,
)
