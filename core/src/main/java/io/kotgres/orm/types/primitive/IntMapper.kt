package io.kotgres.orm.types.primitive

import io.kotgres.orm.types.primitive.base.ImplicitNullMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class IntMapper(nullable: Boolean = false) : ImplicitNullMapper<Int>(Int::class, nullable) {
    override val postgresTypes: List<String>?
        get() = listOf("tinyint", "smallinteger", "integer")

    override fun fromSqlNative(resultSet: ResultSet, position: Int): Int {
        return resultSet.getInt(position)
    }

    override fun toSql(value: Int, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setInt(position, value)
    }
}
