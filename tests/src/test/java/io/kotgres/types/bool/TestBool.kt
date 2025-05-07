package io.kotgres.types.bool

import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.dao.NoPrimaryKeyDao
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.bool.classes.BoolAsBoolean
import io.kotgres.types.bool.classes.BoolAsInt
import io.kotgres.types.bool.classes.BoolAsString
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertTrue

const val TYPE_BOOL_TEST_TABLE = "table_with_bool"

class TestBool : KotgresTest() {



    private val boolAsBooleanDao: PrimaryKeyDao<BoolAsBoolean, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }
    private val boolAsIntDao: PrimaryKeyDao<BoolAsInt, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }
    private val boolAsStringDao: PrimaryKeyDao<BoolAsString, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `insert works with class that has a Boolean`() {
        val result = boolAsBooleanDao.insert(BoolAsBoolean(-1, true))
        assertTrue(result.id > 0)
    }

    @Test
    fun `insert throws with class that has an int`() {
        val error = assertThrows<PSQLException> {
            boolAsIntDao.insert(BoolAsInt(-1, 1))
        }
        assertContains(error.message!!, "column \"bool\" is of type boolean but expression is of type integer")
    }

    @Test
    fun `insert throws with class that has a string`() {
        val error = assertThrows<PSQLException> {
            boolAsStringDao.insert(BoolAsString(-1, "true"))
        }
        assertContains(
            error.message!!,
            "column \"bool\" is of type boolean but expression is of type character varying",
        )
    }
}
