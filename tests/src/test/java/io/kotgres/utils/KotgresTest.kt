package io.kotgres.utils

import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import java.sql.Connection

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class KotgresTest {

    protected lateinit var kotgresConnectionPool: AbstractKotgresConnectionPool
    protected lateinit var conn: Connection

    @BeforeAll
    fun beforeAll() {
        kotgresConnectionPool = createTestKotgresConnection()
        conn = kotgresConnectionPool.getConnection()
        setUpTest()
    }

    open fun setUpTest() {
        // To be used from tests (optionally)
    }

    @AfterAll
    fun afterAll() {
        tearDownTest()
        println("Closing connection...")
        conn.close()
        kotgresConnectionPool.close()
    }

    open fun tearDownTest() {
        // To be used from tests (optionally)
    }
}
