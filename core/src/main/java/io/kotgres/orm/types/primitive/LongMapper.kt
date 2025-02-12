package io.kotgres.orm.types.primitive

import io.kotgres.orm.types.primitive.base.ImplicitNullMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class LongMapper(nullable: Boolean = false) : ImplicitNullMapper<Long>(Long::class, nullable) {

    override val postgresTypes: List<String>?
        get() = listOf("bigint")

    override fun fromSqlNative(resultSet: ResultSet, position: Int): Long {
        return resultSet.getLong(position)
    }

    override fun toSql(value: Long, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setLong(position, value)
    }
}
