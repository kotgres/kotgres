package io.kotgres.orm.types.psql.base

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

/**
 * Mapper for objects that JDBC already knows how to convert, but cannot infer SQL type
 */
abstract class PsqlObjectMapperWithExplicitType<T : Any>(private val tClass: KClass<T>, nullable: Boolean = false) :
    AbstractMapper<T>(tClass, nullable) {

    abstract val explicitType: Int

    override fun fromSql(resultSet: ResultSet, position: Int): T? {
        return resultSet.getObject(position, tClass.java)
    }

    override fun toSql(value: T, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        preparedStatement.setObject(position, value, explicitType)
    }
}
