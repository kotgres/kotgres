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


class KotgresConnectionRunSelectQueryReturningLongTest : KotgresTest() {

    @Test
    fun `works for one query and returns expected result`() {
        val result = kotgresConnectionPool.runSelectQueryReturningOne<Long>("SELECT (1+1)::bigint")
        assertEquals(2, result)
    }

    @Test
    fun `works for one query that has no results`() {
        val result = kotgresConnectionPool.runSelectQueryReturningOne<Long>(SELECT_QUERY_WITH_NO_RESULTS)
        assertNull(result)
    }

    @Test
    fun `throws for update query`() {
        assertThrows<KotgresNoResultsException> {
            kotgresConnectionPool.runSelectQueryReturningOne<Long>(UPDATE_QUERY)
        }
    }

    @Test
    fun `throws for two queries in same line`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningOne<Long>(TWO_NUMER_QUERIES_IN_ONE_LINE)
        }
    }

    @Test
    fun `throws for two queries in different lines`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningOne<Long>(TWO_NUMBER_QUERIES_IN_TWO_LINES)
        }
    }

    @Test
    fun `throws for incorrect query`() {
        val error = assertThrows<PSQLException> {
            kotgresConnectionPool.runSelectQueryReturningOne<Long>(INCORRECT_QUERY)
        }
        assertTrue(error.message!!.contains("syntax error at or near \"SALACT\""))
    }

}