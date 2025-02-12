package io.kotgres.conn

import io.kotgres.conn.constants.*
import io.kotgres.orm.exceptions.query.KotgresNoResultsException
import io.kotgres.orm.exceptions.query.KotgresQueryMultipleResultsException
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue



class KotgresConnectionRunSelectQueryReturningStringTest : KotgresTest() {

    @Test
    fun `works for one query and returns expected result`() {
        val result = kotgresConnectionPool.runSelectQueryReturningOne<String>("SELECT 'hello'")
        assertEquals("hello", result)
    }

    @Test
    fun `works for one query that has no results`() {
        val result = kotgresConnectionPool.runSelectQueryReturningOne<String>(SELECT_QUERY_WITH_NO_RESULTS)
        assertNull(result)
    }

    @Test
    fun `throws for update query`() {
        assertThrows<KotgresNoResultsException> {
            kotgresConnectionPool.runSelectQueryReturningOne<String>(UPDATE_QUERY)
        }
    }

    @Test
    fun `throws for two queries in same line`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningOne<String>("SELECT 'hello'; SELECT 'bye';")
        }
    }

    @Test
    fun `throws for two queries in different lines`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningOne<String>(
                """
SELECT 'hello';
SELECT 'bye';
""".trimIndent(),
            )
        }
    }

    @Test
    fun `throws for incorrect query`() {
        val error = assertThrows<PSQLException> {
            kotgresConnectionPool.runSelectQueryReturningOne<String>(INCORRECT_QUERY)
        }
        assertTrue(error.message!!.contains("syntax error at or near \"SALACT\""))
    }


}
