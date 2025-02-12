package io.kotgres.orm.types.psql.builtin

import io.kotgres.orm.types.psql.base.PsqlObjectMapper
import java.time.LocalDateTime

class LocalDateTimeMapper(nullable: Boolean = false) : PsqlObjectMapper<LocalDateTime>(LocalDateTime::class, nullable) {
    override val postgresTypes: List<String>?
        get() = listOf("timestamp")
}
