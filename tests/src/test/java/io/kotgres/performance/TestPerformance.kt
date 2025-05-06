package io.kotgres.performance

import io.kotgres.classes.user.UserWithId
import io.kotgres.createTestKotgresConnection
import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.generated.dao.UserWithIdDao
import io.kotgres.utils.randomNumber
import io.kotgres.utils.randomString
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.time.LocalDateTime
import kotlin.system.measureTimeMillis
import kotlin.test.assertEquals


class TestPerformance {

    lateinit var conn: AbstractKotgresConnectionPool

    @BeforeAll
    fun beforeAll() {
        conn = createTestKotgresConnection()
    }

    @AfterAll
    fun afterAll() {
        println("Closing connection...")
    }

    val ENTITIES_COUNT = 1_000

    //    @Test
    fun `inserting simple entities with 4 columns sequentially`() {
        val dao = UserWithIdDao(conn)

        // warm internal caches (i.e. for reflection)
        insertRandomUserWithId(dao)

        Thread.sleep(1_000)

        val executionTimeMs = measureTimeMillis {
            for (i in 1..ENTITIES_COUNT) {
                insertRandomUserWithId(dao)
            }
        }

        printTestResults(executionTimeMs)
    }

    //    @Test
    fun `getting simple entities with 4 columns sequentially`() {
        val dao = UserWithIdDao(conn)

        // warm internal caches (i.e. for reflection)
        dao.runSelect(dao.selectQuery().limit(1))

        Thread.sleep(1_000)

        val executionTimeMs = measureTimeMillis {
            for (i in 1..ENTITIES_COUNT) {
                dao.runSelect(dao.selectQuery().limit(1))
            }
        }

        printTestResults(executionTimeMs)
    }

    //    @Test
    fun `getting simple entities with 4 columns in one query`() {
        val dao = UserWithIdDao(conn)

        // warm internal caches (i.e. for reflection)
        dao.runSelect(dao.selectQuery().limit(1))

        Thread.sleep(1_000)

        val executionTimeMs = measureTimeMillis {
            val users = dao.runSelect(dao.selectQuery().limit(ENTITIES_COUNT))
            assertEquals(ENTITIES_COUNT, users.size)
        }

        printTestResults(executionTimeMs)
    }

    private fun printTestResults(executionTimeMs: Long) {
        val executionTimeSeconds = executionTimeMs / 1_000f

        println("TEST COMPLETED:")
        println("-> Entities inserted: $ENTITIES_COUNT")
        println("-> Execution time: ${executionTimeSeconds}s")
        println("-> Time per entity: ${executionTimeMs / ENTITIES_COUNT.toFloat() / 1_000f}ms")
    }

    private fun insertRandomUserWithId(dao: UserWithIdDao) {
        val user = UserWithId(-1, randomString(), randomNumber(), LocalDateTime.now())
        dao.insert(user)
    }
}
