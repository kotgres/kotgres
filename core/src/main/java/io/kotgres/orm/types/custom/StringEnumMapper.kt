package io.kotgres.orm.types.custom

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * Enum in DB but String in code
 */
class StringEnumMapper(nullable: Boolean = false) : AbstractMapper<String>(null, nullable) {

    override val postgresTypes: List<String>?
        get() = null

    // this is actually unused!
    override fun fromSql(resultSet: ResultSet, position: Int): String? {
        return resultSet.getString(position)
    }

    override fun toSql(value: String, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setObject(position, value, Types.OTHER)
    }
}
