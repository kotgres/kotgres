package io.kotgres.orm.types.psql.builtin

import io.kotgres.orm.types.psql.base.PsqlObjectMapperWithExplicitType
import java.sql.Types
import java.util.Date

class DateMapper(nullable: Boolean = false) : PsqlObjectMapperWithExplicitType<Date>(Date::class, nullable) {

    override val explicitType: Int
        get() = Types.TIMESTAMP

    // we want the default to be LocalDateTime
    override val postgresTypes: List<String>?
        get() = null
}
