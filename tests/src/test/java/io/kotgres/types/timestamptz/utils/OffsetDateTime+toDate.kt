package io.kotgres.types.timestamptz.utils

import java.time.OffsetDateTime
import java.util.Date

fun OffsetDateTime.toDate(): Date {
    val instant = this.toInstant()
    return Date.from(instant)
}
