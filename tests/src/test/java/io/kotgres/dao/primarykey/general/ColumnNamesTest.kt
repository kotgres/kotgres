package io.kotgres.dao.primarykey.general

import io.kotgres.dao.primarykey.general.classes.ColumnWithChangedName
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertEquals

const val TEST_COLUMN_NAMES_TABLE_NAME = "column_with_changed_name"


class ColumnNamesTest : KotgresTest() {

    private val columnWithChangedNameDao: PrimaryKeyDao<ColumnWithChangedName, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `inserting and updating works`() {
        val columnValue = 123
        val entity = ColumnWithChangedName(-1, columnValue)

        val insertedEntity = columnWithChangedNameDao.insert(entity)
        val dbEntity = columnWithChangedNameDao.getByPrimaryKey(insertedEntity.id)
        assertEquals(columnValue, dbEntity!!.incorrectName)

        val newColumnValue = 235
        dbEntity.incorrectName = newColumnValue
        columnWithChangedNameDao.update(dbEntity)

        val dbEntityAfterUpdate = columnWithChangedNameDao.getByPrimaryKey(insertedEntity.id)
        assertEquals(newColumnValue, dbEntityAfterUpdate!!.incorrectName)
    }
}
