package io.kotgres.orm.types.primitive.base

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * Mapper for those types where JDBC returns a falsy value instead of null (i.e. 0 for Integer)
 * We need to check wasNull to ensure we don't mess up those cases
 */
abstract class ImplicitNullMapper<T : Any?>(tClass: KClass<*>? = null, nullable: Boolean = false) :
    AbstractMapper<T>(tClass, nullable) {

    // TODO the postfix Native does not make a lot of sense. find a better name
    abstract fun fromSqlNative(resultSet: ResultSet, position: Int): T

    override fun fromSql(resultSet: ResultSet, position: Int): T? {
        val result = fromSqlNative(resultSet, position)
        return if (resultSet.wasNull()) {
            null
        } else {
            result
        }
    }

    override fun toSql(value: T, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setObject(position, value)
    }
}
