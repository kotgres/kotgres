package io.kotgres.types.real

import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.real.classes.RealAsFloat
import io.kotgres.types.real.classes.RealAsInt
import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertTrue

const val TYPE_REAL_TEST_TABLE = "table_with_real"


class RealTest : KotgresTest() {

    private val realAsFloatDao: PrimaryKeyDao<RealAsFloat, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }
    private val realAsIntDao: PrimaryKeyDao<RealAsInt, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `insert works with class that has a float`() {
        val result = realAsFloatDao.insert(RealAsFloat(-1, 1.0f))
        assertTrue(result.id > 0)
    }

    @Test
    fun `insert works with class that has an int`() {
        val result = realAsIntDao.insert(RealAsInt(-1, 1))
        assertTrue(result.id > 0)
    }

    @Test
    fun `get works with class that has an int if the float has decimals`() {
        val result = realAsFloatDao.insert(RealAsFloat(-1, 1.1111333f))

        val resultWithInt = realAsIntDao.getByPrimaryKey(result.id)
        assertTrue(resultWithInt!!.real == 1)
    }
}
