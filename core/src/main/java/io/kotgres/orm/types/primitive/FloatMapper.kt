package io.kotgres.orm.types.primitive

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class FloatMapper(nullable: Boolean = false) : AbstractMapper<Float>(Float::class, nullable) {

    override val postgresTypes: List<String>?
        get() = listOf("real")

    override fun fromSql(resultSet: ResultSet, position: Int): Float? {
        return resultSet.getFloat(position)
    }

    override fun toSql(value: Float, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setFloat(position, value)
    }
}
