package io.kotgres.orm.types.base

import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import kotlin.reflect.KClass

/**
 * @param tClass: class that is mapped to
 * @param nullable: whether mapper allows null values or not
 */
abstract class CustomMapper<T>(tClass: KClass<*>?) : AbstractMapper<T>(tClass, true) {

    abstract fun fromSql(string: String): T?
    abstract fun toSql(value: T): String?

    override fun fromSql(resultSet: ResultSet, position: Int): T? {
        val value = resultSet.getString(position)
        if (resultSet.wasNull()) return null
        return fromSql(value)
    }

    override fun toSql(value: T, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setObject(position, toSql(value), Types.OTHER)
    }

}