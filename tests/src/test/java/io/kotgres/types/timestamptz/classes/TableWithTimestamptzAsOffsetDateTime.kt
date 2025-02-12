package io.kotgres.types.timestamptz.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.timestamptz.TEST_TIMESTAMPTZ_TABLE
import java.time.OffsetDateTime

@Table(name = TEST_TIMESTAMPTZ_TABLE)
data class TableWithTimestamptzAsOffsetDateTime(
    @PrimaryKey
    @Generated
    val id: Int,
    val date: OffsetDateTime,
)
