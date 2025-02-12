package io.kotgres.dao.primarykey.general

import io.kotgres.classes.user.UserWithId
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.exceptions.dao.KotgresColumnNotFoundInQueryResultException
import io.kotgres.orm.exceptions.query.KotgresNoResultsException
import io.kotgres.orm.exceptions.query.KotgresQueryMultipleResultsException
import io.kotgres.orm.exceptions.query.KotgresUnexpectedResultsException
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomString
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.assertThrows
import kotlin.test.Test
import kotlin.test.assertEquals

private const val SELECT_QUERY = "SELECT * FROM users_with_id LIMIT 1"
private const val SELECT_QUERY_WRONG_TABLE = "SELECT * FROM countries LIMIT 1"
private val INSERT_QUERY =
    "INSERT INTO users_with_id (name, age, date_created) VALUES ('${randomString(16)}', 30, '2024-12-28 10:00:00');"
private const val UPDATE_QUERY = "UPDATE users_with_id SET age=99 WHERE 1=2"
private const val DELETE_QUERY = "DELETE FROM users_with_id WHERE 1=2"


class TestRawQueriesErrors : KotgresTest() {

    private val userDao: PrimaryKeyDao<UserWithId, Int> by lazy {
        DaoManager.getPrimaryKeyDao(
            UserWithId::class.java,
            kotgresConnectionPool
        )
    }

    @Nested
    inner class selectQuery {
        @Test
        fun `trying to runSelect for a select on an incorrect table`() {
            val exception = assertThrows<KotgresColumnNotFoundInQueryResultException> {
                userDao.runSelect(SELECT_QUERY_WRONG_TABLE)
            }
            assertEquals("id", exception.columnName)

        }

        @Test
        fun `trying to runSelect for a select works`() {
            val result = userDao.runSelect(SELECT_QUERY)
            assertEquals(1, result.size)
        }

        @Test
        fun `trying to runInsert for a select throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runInsert(SELECT_QUERY)
            }
        }

        @Test
        fun `trying to runUpdate for a select throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runUpdate(SELECT_QUERY)
            }
        }

        @Test
        fun `trying to runDelete for a select throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runDelete(SELECT_QUERY)
            }
        }

        @Test
        fun `trying to runSelect for two queries throws`() {
            assertThrows<KotgresQueryMultipleResultsException> {
                userDao.runSelect("SELECT 1;$SELECT_QUERY")
            }
        }

        @Test
        fun `trying to runSelect for an select and an insert query throws`() {
            assertThrows<KotgresQueryMultipleResultsException> {
                userDao.runSelect("$SELECT_QUERY;$INSERT_QUERY")
            }
        }

        @Test
        fun `trying to runSelect for an insert and a select query throws`() {
            assertThrows<KotgresNoResultsException> {
                userDao.runSelect("$INSERT_QUERY;$SELECT_QUERY")
            }
        }

    }

    @Nested
    inner class insertQuery {
        @Test
        fun `trying to runSelect for a insert throws`() {
            assertThrows<KotgresNoResultsException> {
                userDao.runSelect(INSERT_QUERY)
            }
        }

        @Test
        fun `trying to runInsert for a insert works`() {
            val result = userDao.runInsert(INSERT_QUERY)
            assertEquals(1, result)
        }

        @Test
        fun `trying to runUpdate for a insert works`() {
            val result = userDao.runUpdate(INSERT_QUERY)
            assertEquals(1, result)
        }

        @Test
        fun `trying to runDelete for a insert works`() {
            val result = userDao.runDelete(INSERT_QUERY)
            assertEquals(1, result)
        }

        @Test
        fun `trying to runInsert with two statements works, but only result from last one is returned`() {
            val result = userDao.runInsert("$INSERT_QUERY;$INSERT_QUERY")
            assertEquals(1, result)
        }

        @Test
        fun `trying to runInsert for one select and one insert statements throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runInsert("$SELECT_QUERY;$INSERT_QUERY")
            }
        }

        @Test
        fun `trying to runInsert for one insert and one select statements throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runInsert("$INSERT_QUERY;$SELECT_QUERY")
            }
        }
    }

    @Nested
    inner class updateQuery {
        @Test
        fun `trying to runSelect for an update throws`() {
            assertThrows<KotgresNoResultsException> {
                userDao.runSelect(UPDATE_QUERY)
            }
        }

        @Test
        fun `trying to runInsert for an update works`() {
            val result = userDao.runInsert(UPDATE_QUERY)
            assertEquals(0, result)
        }

        @Test
        fun `trying to runUpdate for an update works`() {
            val result = userDao.runUpdate(UPDATE_QUERY)
            assertEquals(0, result)
        }

        @Test
        fun `trying to runDelete for an update works`() {
            val result = userDao.runDelete(UPDATE_QUERY)
            assertEquals(0, result)
        }

        @Test
        fun `trying to runUpdate with two statements works, but only result from last one is returned`() {
            val result = userDao.runUpdate("$UPDATE_QUERY;$UPDATE_QUERY")
            assertEquals(0, result)
        }

        @Test
        fun `trying to runUpdate for one select and one update statements throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runUpdate("$SELECT_QUERY;$UPDATE_QUERY")
            }
        }

        @Test
        fun `trying to runUpdate for one update and one select statements throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runUpdate("$UPDATE_QUERY;$SELECT_QUERY")
            }
        }
    }

    @Nested
    inner class deleteQuery {
        @Test
        fun `trying to runSelect for a delete throws`() {
            assertThrows<KotgresNoResultsException> {
                userDao.runSelect(DELETE_QUERY)
            }
        }

        @Test
        fun `trying to runInsert for a delete works`() {
            val result = userDao.runInsert(DELETE_QUERY)
            assertEquals(0, result)
        }

        @Test
        fun `trying to runUpdate for a delete works`() {
            val result = userDao.runUpdate(DELETE_QUERY)
            assertEquals(0, result)
        }

        @Test
        fun `trying to runDelete for a delete works`() {
            val result = userDao.runDelete(DELETE_QUERY)
            assertEquals(0, result)
        }


        @Test
        fun `trying to runDelete for a delete works with two delete statements works, but only result from last one is returned`() {
            val result = userDao.runDelete("$DELETE_QUERY;$DELETE_QUERY")
            assertEquals(0, result)
        }

        @Test
        fun `trying to runDelete for one select and one delete statements throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runDelete("$SELECT_QUERY;$DELETE_QUERY")
            }
        }

        @Test
        fun `trying to runDelete for one delete and one select statements throws`() {
            assertThrows<KotgresUnexpectedResultsException> {
                userDao.runDelete("$DELETE_QUERY;$SELECT_QUERY")
            }
        }
    }


}
