package io.kotgres.types.timestamp.utils

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.Date

private val zoneOffset = ZonedDateTime.now(ZoneId.systemDefault()).offset

fun LocalDateTime.toDate(): Date {
    val instant = this.toInstant(zoneOffset)
    return Date.from(instant)
}
