package io.kotgres.dao.primarykey.classformat

import io.kotgres.dao.primarykey.classformat.classes.UserWithMethod
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class WithMethodTest : KotgresTest() {

    private val userWithMethodDao: PrimaryKeyDao<UserWithMethod, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `can insert user and update it with PrimaryKeyDao`() {
        val newAge = 1235

        val user = UserWithMethod(-1, "mohamed", 53, LocalDateTime.now())
        val dbUser = userWithMethodDao.insert(user)

        dbUser.age = newAge
        userWithMethodDao.update(dbUser)

        val dbNewUser = userWithMethodDao.getByPrimaryKey(dbUser.id!!)!!
        assertEquals(dbNewUser.age, newAge)
    }
}
