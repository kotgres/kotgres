package io.kotgres.dao.noprimarykey

import io.kotgres.classes.user.User
import io.kotgres.dsl.extensions.raw
import io.kotgres.dsl.operators.eq
import io.kotgres.dsl.operators.greaterEq
import io.kotgres.dsl.select
import io.kotgres.orm.dao.NoPrimaryKeyDao
import io.kotgres.orm.exceptions.dao.KotgresDaoUnexpectedReturningException
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomString
import org.junit.jupiter.api.assertThrows
import java.sql.ResultSet
import java.sql.Statement
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class BasicNoPkeyTest : KotgresTest() {

    private val userDao: NoPrimaryKeyDao<User> by lazy {
        DaoManager.getNoPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `mapQueryResult does not crash`() {
        userDao.insert(User(-1, randomString(), LocalDateTime.now()))

        val st: Statement = conn.createStatement()
        val rs: ResultSet = st.executeQuery("SELECT * FROM users")
        while (rs.next()) {
            userDao.mapQueryResult(rs)
        }
        rs.close()
        st.close()
    }

    @Test
    fun `getAll works as expected`() {
        userDao.insert(User(-1, randomString(), LocalDateTime.now()))
        val allUsers = userDao.getAll()
        assertTrue(allUsers.isNotEmpty())
    }

    @Test
    fun `can get users over 18 with DSL`() {
        val adultUsersRaw = userDao.runSelect("SELECT * FROM users WHERE age >= 18")

        val adultUsersQuery = select("*")
            .from("users")
            .where("age" greaterEq 18)
        val adultUsersDsl = userDao.runSelect(adultUsersQuery)
        adultUsersDsl.forEach { user ->
            assertTrue(user.age!! >= 18)
        }

        assertEquals(adultUsersRaw.size, adultUsersDsl.size)
    }

    @Test
    fun `can update all users`() {
        val updateQuery = userDao.updateQuery()
            .set("age", 100)
            .where("age" greaterEq 10000)

        val updatedRows = userDao.runUpdate(updateQuery)

        assertEquals(0, updatedRows)
    }

    @Test
    fun `can use insert query`() {
        val insertQuery = userDao.insertQuery()
            .columns("age", "name", "date_created")
            .value(listOf(18, randomString(), "now()".raw))

        val insertedRows = userDao.runInsert(insertQuery)

        assertEquals(1, insertedRows)
    }

    @Test
    fun `can use delete query`() {
        val user = User(123, randomString(), LocalDateTime.now())
        userDao.insert(user)

        val deleteQuery = userDao.deleteQuery()
            .where("name" eq user.name!!)

        val deletedRows = userDao.runDelete(deleteQuery)

        assertEquals(1, deletedRows)
    }

    @Test
    fun `throw if a returning is passed to an insert statement`() {
        assertThrows<KotgresDaoUnexpectedReturningException> {
            userDao.runInsert(
                userDao.insertQuery().defaultValues().returning("b")
            )
        }
    }

    @Test
    fun `throw if a returning is passed to an update statement`() {
        assertThrows<KotgresDaoUnexpectedReturningException> {
            userDao.runDelete(
                userDao.deleteQuery().returning("b")
            )
        }
    }

}
