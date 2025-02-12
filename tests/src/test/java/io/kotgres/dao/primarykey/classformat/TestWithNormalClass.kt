package io.kotgres.dao.primarykey.classformat

import io.kotgres.dao.primarykey.classformat.classes.UserWithNormalClass
import io.kotgres.orm.dao.UserWithNormalClassDao
import io.kotgres.utils.KotgresTest
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals

class TestWithNormalClass : KotgresTest() {

    private lateinit var userWithNormalClassDao: UserWithNormalClassDao

    override fun setUpTest() {
        userWithNormalClassDao = UserWithNormalClassDao(kotgresConnectionPool)
    }

    @Test
    fun `can insert user and update it with PrimaryKeyDao`() {
        val newAge = 1235

        val user = UserWithNormalClass(-1, "mohamed", 53, LocalDateTime.now())
        val dbUser = userWithNormalClassDao.insert(user)

        dbUser.age = newAge
        userWithNormalClassDao.update(dbUser)

        val dbNewUser = userWithNormalClassDao.getByPrimaryKey(dbUser.id!!)!!
        assertEquals(dbNewUser.age, newAge)
    }
}
