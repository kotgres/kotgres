package io.kotgres.dao.noprimarykey

import io.kotgres.classes.user.User
import io.kotgres.dsl.operators.greaterEq
import io.kotgres.dsl.select
import io.kotgres.orm.dao.NoPrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class TestBindingsInRawQueries : KotgresTest() {

    private val userDao:NoPrimaryKeyDao<User> by lazy {
        DaoManager.getNoPrimaryKeyDao(User::class.java, kotgresConnectionPool)
    }

    @Test
    fun `can get users over 18 with raw query and binding`() {

        val adultUsersRaw = userDao.runSelect("SELECT * FROM users WHERE age >= ?", listOf(null))

        adultUsersRaw.forEach { user ->
            assertTrue(user.age!! >= 18)
        }
    }

    @Test
    fun `can get users over 18 with DSL and binding`() {
        val adultUsersQuery = select("*")
            .from("users")
            .where("age" greaterEq 18)

        val adultUsersDsl = userDao.runSelect(adultUsersQuery)

        adultUsersDsl.forEach { user ->
            assertTrue(user.age!! >= 18)
        }
    }

    @Test
    fun `can update with raw query`() {
        val adultUsersRaw = userDao.runUpdate("UPDATE users SET age = 0 WHERE age >= ?", bindings = listOf(999))

        assertEquals(0, adultUsersRaw)
    }

}
