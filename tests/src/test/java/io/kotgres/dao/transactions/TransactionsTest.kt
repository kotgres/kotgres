package io.kotgres.dao.transactions

import io.kotgres.classes.country.Country
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.exceptions.transaction.KotgresTransactionFinalizedException
import io.kotgres.orm.exceptions.transaction.KotgresTransactionNotFinalizedException
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomString
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertNull

class TransactionsTest : KotgresTest() {

    private val countryDao: PrimaryKeyDao<Country, String> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Nested
    inner class getNewTransaction {

        @Test
        fun `works with commit`() {
            val trx = kotgresConnectionPool.createTransaction()
            trx.runQueryVoid("SELECT 1")
            trx.runQueryVoid("SELECT 2")
            trx.runQueryVoid("SELECT 3")
            trx.commit()
        }

        @Test
        fun `works with rollback`() {
            val trx = kotgresConnectionPool.createTransaction()
            trx.runQueryVoid("SELECT 1")
            trx.runQueryVoid("SELECT 2")
            trx.runQueryVoid("SELECT 3")
            trx.rollback()
        }

        @Test
        fun `throws if commit twice`() {
            val trx = kotgresConnectionPool.createTransaction()
            trx.runQueryVoid("SELECT 1")
            trx.runQueryVoid("SELECT 2")
            trx.runQueryVoid("SELECT 3")
            trx.commit()
            assertThrows<KotgresTransactionFinalizedException> {
                trx.commit()
            }
        }

        @Test
        fun `throws if rollback twice`() {
            val trx = kotgresConnectionPool.createTransaction()
            trx.runQueryVoid("SELECT 1")
            trx.runQueryVoid("SELECT 2")
            trx.runQueryVoid("SELECT 3")
            trx.rollback()
            assertThrows<KotgresTransactionFinalizedException> {
                trx.rollback()
            }
        }
    }


    @Nested
    inner class useTransaction {
        @Test
        fun `works with commit`() {
            kotgresConnectionPool.useTransaction { trx ->
                trx.runQueryVoid("SELECT 1")
                trx.runQueryVoid("SELECT 2")
                trx.runQueryVoid("SELECT 3")
                trx.commit()
            }
        }

        @Test
        fun `works with rollback`() {
            kotgresConnectionPool.useTransaction { trx ->
                trx.runQueryVoid("SELECT 1")
                trx.runQueryVoid("SELECT 2")
                trx.runQueryVoid("SELECT 3")
                trx.rollback()
            }
        }

        @Test
        fun `throws is transaction is not finalized`() {
            assertThrows<KotgresTransactionNotFinalizedException> {
                kotgresConnectionPool.useTransaction { trx ->
                    trx.runQueryVoid("SELECT 1")
                    trx.runQueryVoid("SELECT 2")
                    trx.runQueryVoid("SELECT 3")
                }
            }
        }
    }

    @Test
    fun `works with DAOs`() {
        val countryCode = randomString()
        try {
            kotgresConnectionPool.useTransaction { trx ->
                countryDao.insert(Country(countryCode, randomString(), LocalDateTime.now()), trx)
                throw Exception("Mock exception")
            }
        } catch (ignored: Exception) {
            // we ignore to later check if country was actually inserted or not
        }

        assertNull(countryDao.getByPrimaryKey(countryCode))
    }
}
