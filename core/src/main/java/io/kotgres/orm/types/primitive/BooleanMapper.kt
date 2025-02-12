package io.kotgres.orm.types.primitive

import io.kotgres.orm.types.primitive.base.ImplicitNullMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class BooleanMapper(nullable: Boolean = false) : ImplicitNullMapper<Boolean>(Boolean::class, nullable) {

    override val postgresTypes: List<String>?
        get() = listOf("bit", "boolean")

    override fun fromSqlNative(resultSet: ResultSet, position: Int): Boolean {
        return resultSet.getBoolean(position)
    }

    override fun toSql(value: Boolean, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setBoolean(position, value)
    }
}
