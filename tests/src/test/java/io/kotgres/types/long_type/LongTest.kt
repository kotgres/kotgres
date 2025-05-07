package io.kotgres.types.long_type

import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.long_type.classes.TableWithBigInt
import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertEquals

class LongTest : KotgresTest() {

    private val dao: PrimaryKeyDao<TableWithBigInt, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `insert works`() {
        dao.insert(TableWithBigInt(-1, 212389L))
    }

    @Test
    fun `get works`() {
        val value = 128312931L
        val insertResult = dao.insert(TableWithBigInt(-1, value))
        assertEquals(value, insertResult.long)

        val dbEntity = dao.getByPrimaryKey(insertResult.id)!!
        assertEquals(value, dbEntity.long)
    }
}