package io.kotgres.orm.types.primitive.arrays

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet


// TODO: https://stackoverflow.com/questions/17842211/how-to-use-an-arraylist-as-a-prepared-statement-parameter


class StringArrayListMapper(nullable: Boolean = false) : AbstractMapper<List<String>>(List::class, nullable) {

    override val postgresTypes: List<String>
        get() = listOf("text[]")

    override fun fromSql(resultSet: ResultSet, position: Int): List<String>? {
        val value = resultSet.getArray(position) ?: return null

        return (value.array as Array<String>).toList()
    }

    override fun toSql(value: List<String>, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        val list = mutableListOf<String>()
        for(item in value) {
            list.add(item)
        }

        val array = conn.createArrayOf("VARCHAR", list.toTypedArray())
        preparedStatement.setArray(position, array)
    }
}
