package io.kotgres.orm.types.primitive

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class StringMapper(nullable: Boolean = false) : AbstractMapper<String>(String::class, nullable) {

    override val postgresTypes: List<String>?
        get() = listOf("char", "varchar", "longvarchar")

    override fun fromSql(resultSet: ResultSet, position: Int): String? {
        return resultSet.getString(position)
    }

    override fun toSql(value: String, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setString(position, value)
    }
}
