package io.kotgres.orm.types.psql.builtin

import io.kotgres.orm.types.base.AbstractMapper
import java.math.BigDecimal
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet

class BigDecimalMapper(nullable: Boolean = false) : AbstractMapper<BigDecimal>(BigDecimal::class, nullable) {
    override val postgresTypes: List<String>?
        get() = listOf("numeric", "decimal")

    override fun fromSql(resultSet: ResultSet, position: Int): BigDecimal {
        return resultSet.getBigDecimal(position)
    }

    override fun toSql(value: BigDecimal, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setBigDecimal(position, value)
    }
}
