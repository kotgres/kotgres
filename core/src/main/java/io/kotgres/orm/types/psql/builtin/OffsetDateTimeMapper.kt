package io.kotgres.orm.types.psql.builtin

import io.kotgres.orm.types.psql.base.PsqlObjectMapper
import java.time.OffsetDateTime

class OffsetDateTimeMapper(nullable: Boolean = false) :
    PsqlObjectMapper<OffsetDateTime>(OffsetDateTime::class, nullable) {
    override val postgresTypes: List<String>?
        get() = listOf("timestamptz")
}
