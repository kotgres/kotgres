package io.kotgres.daomanager

import io.kotgres.classes.dog.UserInMainSourceSetNoPrimaryKey
import io.kotgres.classes.dog.UserInMainSourceSetWithPrimaryKey
import io.kotgres.classes.user.UserWithId
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.exceptions.dao.KotgresDaoNotFoundException
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test


class TestDaoManager : KotgresTest() {
    @Test
    fun `does not crash when getting existing dao by string (kotlin class)`() {
        val userDao: PrimaryKeyDao<UserWithId, Int> =
            DaoManager.getPrimaryKeyDao(UserWithId::class, kotgresConnectionPool)
    }

    @Test
    fun `does crash when getting existing dao (kotlin class)`() {
        assertThrows<KotgresDaoNotFoundException> {
            val userDao: PrimaryKeyDao<String, Int> = DaoManager.getPrimaryKeyDao(String::class, kotgresConnectionPool)
        }
    }

    @Test
    fun `does not crash when getting existing dao by string (java class)`() {
        val userDao: PrimaryKeyDao<UserWithId, Int> =
            DaoManager.getPrimaryKeyDao(UserWithId::class.java, kotgresConnectionPool)
    }

    @Test
    fun `does crash when getting existing dao (java class)`() {
        assertThrows<KotgresDaoNotFoundException> {
            val userDao: PrimaryKeyDao<String, Int> =
                DaoManager.getPrimaryKeyDao(String::class.java, kotgresConnectionPool)
        }
    }

    @Test
    fun `does crash when getting no primary key dao from another source set`() {
        DaoManager.getNoPrimaryKeyDao(UserInMainSourceSetNoPrimaryKey::class.java, kotgresConnectionPool)
    }

    @Test
    fun `does crash when getting with primary key dao from another source set`() {
        val userDao: PrimaryKeyDao<UserInMainSourceSetWithPrimaryKey, String> =
            DaoManager.getPrimaryKeyDao(UserInMainSourceSetWithPrimaryKey::class.java, kotgresConnectionPool)
    }
}
