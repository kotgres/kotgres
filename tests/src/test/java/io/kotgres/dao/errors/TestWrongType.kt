package io.kotgres.dao.errors

import io.kotgres.classes.user.UserWithWrongAgeType
import io.kotgres.orm.dao.NoPrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals



class TestWrongType : KotgresTest() {

    private val userDao: NoPrimaryKeyDao<UserWithWrongAgeType> by lazy {
        DaoManager.getNoPrimaryKeyDao(UserWithWrongAgeType::class, kotgresConnectionPool)
    }

    @Test
    fun `insert does crash`() {
        val exception = assertThrows<org.postgresql.util.PSQLException> {
            userDao.insert(UserWithWrongAgeType("hello", "error", LocalDateTime.now()))
        }

        assertEquals(
            "ERROR: column \"age\" is of type integer but expression is of type character varying\n" +
                    "  Hint: You will need to rewrite or cast the expression.\n" +
                    "  Position: 56", exception.message
        )
    }
}