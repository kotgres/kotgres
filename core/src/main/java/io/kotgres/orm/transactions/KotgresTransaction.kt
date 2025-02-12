package io.kotgres.orm.transactions

import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.exceptions.transaction.KotgresTransactionFinalizedException
import io.kotgres.orm.exceptions.transaction.KotgresTransactionNotFinalizedException
import java.sql.Connection

class KotgresTransaction(connection: AbstractKotgresConnectionPool) {

    val connection: Connection = connection.getConnection()
    private var isTransactionWrappedUp: Boolean = false


    init {
        this.connection.autoCommit = false

        this.connection.createStatement().use {
            it!!.execute("BEGIN")
        }
    }

    fun verifyIsOpen() {
        if (isTransactionWrappedUp) {
            throw KotgresTransactionFinalizedException()
        }
    }

    fun verifyIsWrappedUp() {
        if (!isTransactionWrappedUp) {
            throw KotgresTransactionNotFinalizedException()
        }
    }

    fun commit() {
        preTransactionWrapUpChecks()
        connection.commit()
        destroyTransaction()
    }

    fun rollback() {
        preTransactionWrapUpChecks()
        connection.rollback()
        destroyTransaction()
    }

    fun runQueryVoid(query: String) {
        val st = connection.createStatement()
        st.use {
            it.execute(query)
        }
    }

    private fun preTransactionWrapUpChecks() {
        if (isTransactionWrappedUp) {
            throw KotgresTransactionFinalizedException()
        }
    }

    private fun destroyTransaction() {
        isTransactionWrappedUp = true
        connection.close()
    }

}