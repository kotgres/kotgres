package io.kotgres.dao.primarykey.general

import io.kotgres.classes.user.UserWithId
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TestNullability : KotgresTest() {

    private val userDao: PrimaryKeyDao<UserWithId, Int> by lazy {
        DaoManager.getPrimaryKeyDao(
            kotgresConnectionPool
        )
    }

    @Test
    fun `can insert null in fields`() {
        val user = UserWithId(-1, null, null, null)
        val insertedUser = userDao.insert(user)

        val dbUser = userDao.getByPrimaryKey(insertedUser.id)!!
        assertNull(dbUser.age)
        assertNull(dbUser.name)
        assertNull(dbUser.dateCreated)
    }

    @Test
    fun `can update null fields`() {
        val newAge = 1235

        val user = UserWithId(-1, null, null, null)
        val insertedUser = userDao.insert(user)

        val newUser = insertedUser.copy(age = newAge)
        userDao.update(newUser)

        val dbUser = userDao.getByPrimaryKey(insertedUser.id)!!
        assertEquals(dbUser.age, newAge)
    }
}
