package io.kotgres.orm.types.primitive

import io.kotgres.orm.types.primitive.base.ImplicitNullMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class ShortMapper(nullable: Boolean = false) : ImplicitNullMapper<Short>(Short::class, nullable) {
    override val postgresTypes: List<String>?
        get() = listOf("smallint")

    override fun fromSqlNative(resultSet: ResultSet, position: Int): Short {
        return resultSet.getShort(position)
    }

    override fun toSql(value: Short, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setShort(position, value)
    }
}
