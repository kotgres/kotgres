package io.kotgres.orm.types.psql.builtin

import io.kotgres.orm.types.psql.base.PsqlObjectMapper
import java.time.LocalTime

class TimeMapper(nullable: Boolean = false) :
    PsqlObjectMapper<LocalTime>(LocalTime::class, nullable) {

    override val postgresTypes: List<String>?
        get() = listOf("time")
}
