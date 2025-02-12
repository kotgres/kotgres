package io.kotgres.dao.primarykey.general

import io.kotgres.dao.primarykey.general.classes.PrimaryKeyWithDifferentName
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

const val TEST_PKEY_NAMES_TABLE_NAME = "primary_key_with_different_name"


class TestDifferentPrimaryKeyName : KotgresTest() {

    private val primaryKeyWithDifferentNameDao: PrimaryKeyDao<PrimaryKeyWithDifferentName, Int> by lazy {
        DaoManager.getPrimaryKeyDao(
            PrimaryKeyWithDifferentName::class.java,
            kotgresConnectionPool
        )
    }

    @Test
    fun `inserting and updating works`() {
        val columnValue = 123
        val entity = PrimaryKeyWithDifferentName(-1, columnValue)

        val insertedEntity = primaryKeyWithDifferentNameDao.insert(entity)
        val dbEntity = primaryKeyWithDifferentNameDao.getByPrimaryKey(insertedEntity.myKotlinId)
        assertEquals(columnValue, dbEntity!!.int)

        val newColumnValue = 235
        dbEntity.int = newColumnValue
        primaryKeyWithDifferentNameDao.update(dbEntity)

        val dbEntityAfterUpdate = primaryKeyWithDifferentNameDao.getByPrimaryKey(insertedEntity.myKotlinId)
        assertEquals(newColumnValue, dbEntityAfterUpdate!!.int)
    }

    @Test
    fun `deleting works`() {
        val entity = PrimaryKeyWithDifferentName(-1, 1)

        val insertedEntity = primaryKeyWithDifferentNameDao.insert(entity)
        val dbEntity = primaryKeyWithDifferentNameDao.getByPrimaryKey(insertedEntity.myKotlinId)
        assertNotNull(dbEntity)

        primaryKeyWithDifferentNameDao.deleteById(dbEntity.myKotlinId)

        val dbEntityAfterDelete = primaryKeyWithDifferentNameDao.getByPrimaryKey(insertedEntity.myKotlinId)
        assertNull(dbEntityAfterDelete)
    }
}
