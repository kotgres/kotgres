package io.kotgres.dao.primarykey.classformat

import io.kotgres.dao.primarykey.classformat.classes.UserWithIdLateInitId
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.exceptions.dao.KotgresCantUpdateNullPrimaryKeyEntityException
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.assertEquals

// TODO re-eanble or remove
class TestLateInit : KotgresTest() {

    private val userDao: PrimaryKeyDao<UserWithIdLateInitId, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

//    @Test
    fun `updating copied entity does not work since id is no in constructor`() {
        val newAge = 1235

        val user = UserWithIdLateInitId("mohamed", 53, LocalDateTime.now())
        val dbUser = userDao.insert(user)

        val newUser = dbUser.copy(age = newAge)

        assertThrows<KotgresCantUpdateNullPrimaryKeyEntityException> {
            userDao.update(newUser)
        }
    }

//    @Test
    fun `trying to update without id throws`() {
        val user = UserWithIdLateInitId("mohamed", 53, LocalDateTime.now())

        assertThrows<KotgresCantUpdateNullPrimaryKeyEntityException> {
            userDao.update(user)
        }
    }

//    @Test
    fun `can insert user and update it with PrimaryKeyDao`() {
        val newAge = 1235

        val user = UserWithIdLateInitId("mohamed", 53, LocalDateTime.now())
        val dbUser = userDao.insert(user)

        dbUser.age = newAge
        userDao.update(dbUser)

        val dbNewUser = userDao.getByPrimaryKey(dbUser.id!!)!!
        assertEquals(dbNewUser.age, newAge)
    }
}
