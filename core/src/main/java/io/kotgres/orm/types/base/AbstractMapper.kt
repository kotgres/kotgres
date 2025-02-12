package io.kotgres.orm.types.base

import io.kotgres.orm.exceptions.dao.KotgresUnexpectedNullException
import io.kotgres.orm.exceptions.internal.KotgresInternalException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types
import kotlin.reflect.KClass

/**
 * @param tClass: class that is mapped to
 * @param nullable: whether mapper allows null values or not
 */
abstract class AbstractMapper<T>(tClass: KClass<*>? = null, private val nullable: Boolean = true) {

    val kotlinClassName: String?
    abstract val postgresTypes: List<String>?

    init {
        kotlinClassName = if (tClass != null) {
            tClass.qualifiedName
                ?: throw KotgresInternalException("Local or anonymous classes cannot be mapped. Please extract it to its own file")
        } else {
            null
        }
    }

    /**
     * PUBLIC METHODS
     */
    // TODO do we need throwIfUnexectedNull here?
    fun getFromResultSetNullable(resultSet: ResultSet, position: Int): T? {
        val value = fromSql(resultSet, position)

        throwIfUnexpectedNull(value)

        return value
    }

    fun getFromResultSet(resultSet: ResultSet, position: Int): T {
        val value = fromSql(resultSet, position)

        throwIfUnexpectedNull(value)

        return value!!
    }

    fun addToStatement(value: Any?, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        if (value != null) {
            return try {
                toSql(value as T, preparedStatement, position, conn)
            } catch (e: ClassCastException) {
                throw KotgresInternalException("Value is not of expected type (${e.message})")
            }
        }

        if (!nullable) throw KotgresInternalException("Got null value when field is not nullable")

        return preparedStatement.setNull(position, Types.NULL)
    }

    /**
     * PROTECTED METHODS
     */
    protected abstract fun fromSql(resultSet: ResultSet, position: Int): T?

    protected abstract fun toSql(value: T, preparedStatement: PreparedStatement, position: Int, conn: Connection)

    /**
     * PRIVATE METHODS
     */
    private fun throwIfUnexpectedNull(value: T?) {
        if (value == null && !nullable) throw KotgresUnexpectedNullException()
    }
}
