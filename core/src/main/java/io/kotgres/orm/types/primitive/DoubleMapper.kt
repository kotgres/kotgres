package io.kotgres.orm.types.primitive

import io.kotgres.orm.types.primitive.base.ImplicitNullMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class DoubleMapper(nullable: Boolean = false) : ImplicitNullMapper<Double>(Double::class, nullable) {

    override val postgresTypes: List<String>?
        get() = listOf("float", "double precision")

    override fun fromSqlNative(resultSet: ResultSet, position: Int): Double {
        return resultSet.getDouble(position)
    }

    override fun toSql(value: Double, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setDouble(position, value)
    }

}
