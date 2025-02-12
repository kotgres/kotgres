package io.kotgres.orm.types.psql.extensions

import io.kotgres.orm.types.psql.base.PsqlObjectMapper
import java.util.UUID

class UuidMapper(nullable: Boolean = false) : PsqlObjectMapper<UUID>(UUID::class, nullable) {
    override val postgresTypes: List<String>?
        get() = listOf("uuid")
}
