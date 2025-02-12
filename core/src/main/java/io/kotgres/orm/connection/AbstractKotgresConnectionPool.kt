package io.kotgres.orm.connection

import io.kotgres.orm.transactions.KotgresTransaction
import java.sql.Connection


abstract class AbstractKotgresConnectionPool : AutoCloseable {

    /**
     * ABSTRACT FIELDS
     */

    abstract fun getConnection(): Connection

    /**
     * PUBLIC METHODS: queries
     */

    fun runQueryVoid(query: String) {
        getConnection().use { conn ->
            val st = conn.createStatement()
            st.use {
                it.execute(query)
            }
        }
    }

    /**
     * PUBLIC METHODS: transactions
     */
    fun useTransaction(block: (trx: KotgresTransaction) -> Unit) {
        val newTransaction = createTransaction()
        block(newTransaction)
        newTransaction.verifyIsWrappedUp()
    }

    fun createTransaction(): KotgresTransaction {
        return KotgresTransaction(this)
    }

    /**
     * PUBLIC METHODS: String query
     */

    /**
     * Returns a single element for a query that returns only one column
     * Only supports types that are natively supported by JDBC
     * Non-exhaustive list: String, Boolean, Int, Double, Long, Date, LocalDateTime, ...
     */
    inline fun <reified T> runSelectQueryReturningOne(query: String): T? {
        return KotgresConnectionUtils.runSelectQueryReturningOne(query, KotgresConnectionUtils::get, this::getConnection)
    }

    /**
     * Returns many element for a query that returns only one column
     * Only supports types that are natively supported by JDBC
     * Non-exhaustive list: String, Boolean, Int, Double, Long, Date, LocalDateTime, ...
     */
    inline fun <reified T> runSelectQueryReturningList(query: String): List<T> {
        return KotgresConnectionUtils.runSelectQueryReturningList(query, KotgresConnectionUtils::get, this::getConnection)
    }


}
