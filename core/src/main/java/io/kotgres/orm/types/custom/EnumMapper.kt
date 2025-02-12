package io.kotgres.orm.types.custom

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

/**
 * Enum in DB and code
 */
class EnumMapper(nullable: Boolean = false) : AbstractMapper<Enum<*>>(Enum::class, nullable) {

    override val postgresTypes: List<String>?
        get() = null

    // this is actually unused!
    override fun fromSql(resultSet: ResultSet, position: Int): Enum<*>? {
        return getFromResultSetInternal(resultSet, position)
    }

    override fun toSql(value: Enum<*>, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setObject(position, value.name.lowercase(), Types.OTHER)
    }

    // TODO fix this hardcoded uppercase
    inline fun <reified T : Enum<*>> getFromResultSetInternal(resultSet: ResultSet, position: Int): T {
        val stringValue = resultSet.getString(position)
        return T::class.java.getDeclaredMethod("valueOf", String::class.java).invoke(null, stringValue.uppercase()) as T
    }
}
