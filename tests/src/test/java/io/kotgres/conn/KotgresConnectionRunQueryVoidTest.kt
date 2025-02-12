package io.kotgres.conn

import io.kotgres.conn.constants.*
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException
import kotlin.test.*


class KotgresConnectionRunQueryVoidTest : KotgresTest() {

    @Test
    fun `works for one query`() {
        kotgresConnectionPool.runQueryVoid("SELECT 1+1")
    }

    @Test
    fun `works for one query that has no results`() {
        kotgresConnectionPool.runQueryVoid(SELECT_QUERY_WITH_NO_RESULTS)
    }

    @Test
    fun `works for two queries in same line`() {
        kotgresConnectionPool.runQueryVoid(TWO_NUMER_QUERIES_IN_ONE_LINE)
    }

    @Test
    fun `works for two queries in different lines`() {
        kotgresConnectionPool.runQueryVoid(TWO_NUMBER_QUERIES_IN_TWO_LINES)
    }

    @Test
    fun `throws for incorrect query`() {
        val error = assertThrows<PSQLException> {
            kotgresConnectionPool.runQueryVoid(INCORRECT_QUERY)
        }
        assertTrue(error.message!!.contains("syntax error at or near \"SALACT\""))
    }


}
