package io.kotgres.orm.dao

import io.kotgres.dsl.extensions.raw
import io.kotgres.dsl.insertInto
import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.dao.model.ValueWithMapper
import io.kotgres.orm.transactions.KotgresTransaction

abstract class NoPrimaryKeyDao<E>(conn: AbstractKotgresConnectionPool) : AbstractDao<E>(conn) {

    /**
     * PUBLIC METHODS
     */

    fun insert(entity: E, trx: KotgresTransaction? = null): Int {
        val bindingWithMapperList = buildBindingsForInsert(entity)

        val updatedRowsCount =
            this.runUpdateExecuteNoPkey(
                insertInto(tableName)
                    .columns(insertColumnsString.raw)
                    .valueRaw(insertBindingString.raw)
                    .toSql(true),
                bindingWithMapperList,
                trx,
            )

        return updatedRowsCount
    }

    /**
     * PRIVATE METHODS
     */

    // TODO code half repeated in AbstractDao/PrimaryKeyDao

    private fun runUpdateExecuteNoPkey(query: String, bindings: List<ValueWithMapper>, trx: KotgresTransaction?): Int {
        return useDaoConnection(trx) { conn ->
            val preparedStatement = conn.prepareStatement(query)
            addBindingsToPreparedStatement(bindings, preparedStatement, conn)
            return@useDaoConnection preparedStatement.executeUpdate()
        }
    }

    private fun runUpdateExecuteNoPkey(query: String, binding: ValueWithMapper, trx: KotgresTransaction?): Int {
        return runUpdateExecuteNoPkey(query, listOf(binding), trx)
    }

    private fun runUpdateExecuteNoPkey(query: String, trx: KotgresTransaction?): Int {
        return runUpdateExecuteNoPkey(query, listOf(), trx)
    }
}
