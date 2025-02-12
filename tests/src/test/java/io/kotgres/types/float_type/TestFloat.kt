package io.kotgres.types.float_type

import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.float_type.classes.TableWithFloat
import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertEquals

class TestFloat : KotgresTest() {

    private val dao: PrimaryKeyDao<TableWithFloat, Int> by lazy {
        DaoManager.getPrimaryKeyDao(TableWithFloat::class, kotgresConnectionPool)
    }

    @Test
    fun `insert works`() {
        dao.insert(TableWithFloat(-1, 1.0f))
    }

    @Test
    fun `get works`() {
        val value = 1.9123712f
        val insertResult = dao.insert(TableWithFloat(-1, value))
        assertEquals(value, insertResult.float)

        val dbEntity = dao.getByPrimaryKey(insertResult.id)!!
        assertEquals(value, dbEntity.float)
    }

}