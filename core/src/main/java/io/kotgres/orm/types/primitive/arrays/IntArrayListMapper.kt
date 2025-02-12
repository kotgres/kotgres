package io.kotgres.orm.types.primitive.arrays

import io.kotgres.orm.types.base.AbstractMapper
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet


// TODO: https://stackoverflow.com/questions/17842211/how-to-use-an-arraylist-as-a-prepared-statement-parameter


class IntArrayListMapper(nullable: Boolean = false) : AbstractMapper<List<Int>>(List::class, nullable) {

    override val postgresTypes: List<String>
        get() = listOf("int[]")

    override fun fromSql(resultSet: ResultSet, position: Int): List<Int>? {
        val value = resultSet.getArray(position) ?: return null

        return (value.array as Array<Int>).toList()
    }

    override fun toSql(value: List<Int>, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
        val list = mutableListOf<Int>()
        for (item in value) {
            list.add(item)
        }

        val array = conn.createArrayOf("INT", list.toTypedArray())
        preparedStatement.setArray(position, array)
    }
}
