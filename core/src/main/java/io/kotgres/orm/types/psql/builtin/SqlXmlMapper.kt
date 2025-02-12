package io.kotgres.orm.types.psql.builtin

import io.kotgres.orm.types.psql.base.PsqlObjectMapper
import java.sql.SQLXML

// TODO test it works
class SqlXmlMapper(nullable: Boolean = false) : PsqlObjectMapper<SQLXML>(SQLXML::class, nullable) {
    override val postgresTypes: List<String>?
        get() = listOf("sqlxml")
}
