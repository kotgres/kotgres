package io.kotgres.types.custom

import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.custom.classes.UserWithWrappedInt
import io.kotgres.types.custom.classes.WrappedInt
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomNumber
import io.kotgres.utils.randomString
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull


class TestCustomMapper : KotgresTest() {

    private val dao: PrimaryKeyDao<UserWithWrappedInt, Int> by lazy {
        DaoManager.getPrimaryKeyDao(
            UserWithWrappedInt::class,
            kotgresConnectionPool
        )
    }

    @Test
    fun `insert, get, update and delete works for not null field`() {
        val initialAge = randomNumber(100_000_000)
        val updatedAge = randomNumber(100_000_000)
        val newEntity = UserWithWrappedInt(
            id = -1,
            age = WrappedInt(initialAge),
            dateCreated = LocalDateTime.now(),
            name = randomString(),
        )
        val insertedEntity = dao.insert(newEntity)
        assertEquals(initialAge, insertedEntity.age!!.int)

        val entityToUpdated = insertedEntity.copy(age = WrappedInt(updatedAge))
        val updatedEntity = dao.update(entityToUpdated)!!
        assertEquals(updatedAge, updatedEntity.age!!.int)

        val entityAfterUpdate = dao.getByPrimaryKey(entityToUpdated.id!!)
        assertEquals(updatedAge, entityAfterUpdate!!.age!!.int)

        dao.deleteById(entityAfterUpdate.id!!)

        val entityAfterDelete = dao.getByPrimaryKey(entityToUpdated.id!!)
        assertNull(entityAfterDelete)
    }

    @Test
    fun `insert, get, update and delete works for initially null field`() {
        val updatedAge = randomNumber(100_000_000)
        val newEntity = UserWithWrappedInt(
            id = -1,
            age = null,
            dateCreated = LocalDateTime.now(),
            name = randomString(),
        )
        val insertedEntity = dao.insert(newEntity)
        assertNull(insertedEntity.age)

        val entityToUpdated = insertedEntity.copy(age = WrappedInt(updatedAge))
        val updatedEntity = dao.update(entityToUpdated)!!
        assertEquals(updatedAge, updatedEntity.age!!.int)

        val entityAfterUpdate = dao.getByPrimaryKey(entityToUpdated.id!!)
        assertEquals(updatedAge, entityAfterUpdate!!.age!!.int)

        dao.deleteById(entityAfterUpdate.id!!)

        val entityAfterDelete = dao.getByPrimaryKey(entityToUpdated.id!!)
        assertNull(entityAfterDelete)
    }

    @Test
    fun `insert, get, update and delete works for finally null field`() {
        val initialAge = randomNumber(100_000_000)
        val newEntity = UserWithWrappedInt(
            id = -1,
            age = WrappedInt(initialAge),
            dateCreated = LocalDateTime.now(),
            name = randomString(),
        )
        val insertedEntity = dao.insert(newEntity)
        assertEquals(initialAge, insertedEntity.age!!.int)

        val entityToUpdated = insertedEntity.copy(age = null)
        val updatedEntity = dao.update(entityToUpdated)!!
        assertNull(updatedEntity.age)

        val entityAfterUpdate = dao.getByPrimaryKey(entityToUpdated.id!!)
        assertNull(entityAfterUpdate!!.age)

        dao.deleteById(entityAfterUpdate.id!!)

        val entityAfterDelete = dao.getByPrimaryKey(entityToUpdated.id!!)
        assertNull(entityAfterDelete)
    }

}