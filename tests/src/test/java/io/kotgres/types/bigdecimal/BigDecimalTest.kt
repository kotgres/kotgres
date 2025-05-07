package io.kotgres.types.bigdecimal

import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.bigdecimal.classes.TableWithDecimal
import io.kotgres.types.bigdecimal.classes.TableWithNumeric
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.Nested
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals

class BigDecimalTest: KotgresTest() {


    @Nested
    inner class decimal {

        private val dao: PrimaryKeyDao<TableWithDecimal, Int> by lazy {
            DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
        }

        @Test
        fun `insert works`() {
            dao.insert(TableWithDecimal(-1, BigDecimal.TEN))
        }

        @Test
        fun `get works`() {
            val value = BigDecimal.valueOf(1.12381236712386)
            val insertResult = dao.insert(TableWithDecimal(-1, value))
            assertEquals(value, insertResult.decimal)

            val dbEntity = dao.getByPrimaryKey(insertResult.id)!!
            assertEquals(value, dbEntity.decimal)
        }
    }

    @Nested
    inner class numeric {

        private val dao: PrimaryKeyDao<TableWithNumeric, Int> by lazy {
            DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
        }


        @Test
        fun `insert works`() {
            dao.insert(TableWithNumeric(-1, BigDecimal.TEN))
        }

        @Test
        fun `get works`() {
            val value = BigDecimal.valueOf(1.12381236712386)
            val insertResult = dao.insert(TableWithNumeric(-1, value))
            assertEquals(value, insertResult.numeric)

            val dbEntity = dao.getByPrimaryKey(insertResult.id)!!
            assertEquals(value, dbEntity.numeric)
        }
    }
}