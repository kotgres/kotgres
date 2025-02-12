package io.kotgres.conn

import io.kotgres.conn.constants.INCORRECT_QUERY
import io.kotgres.conn.constants.SELECT_QUERY_WITH_NO_RESULTS
import io.kotgres.conn.constants.TWO_NUMBER_QUERIES_IN_TWO_LINES
import io.kotgres.conn.constants.TWO_NUMER_QUERIES_IN_ONE_LINE
import io.kotgres.conn.constants.UPDATE_QUERY
import io.kotgres.orm.exceptions.query.KotgresNoResultsException
import io.kotgres.orm.exceptions.query.KotgresQueryMultipleResultsException
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val SELECT_MULTIPLE_BOOLEANS_QUERY = "SELECT * FROM (VALUES (true), (false), (true)) AS t(b);"
private val SELECT_MULTIPLE_BOOLEANS_RESULTS = listOf(true, false, true)


class KotgresConnectionrunSelectQueryReturningListTest : KotgresTest() {


    @Test
    fun `works for expected query and returns expected result`() {
        val result =
            kotgresConnectionPool.runSelectQueryReturningList<Boolean>(SELECT_MULTIPLE_BOOLEANS_QUERY)
        assertEquals(SELECT_MULTIPLE_BOOLEANS_RESULTS, result)
    }

    @Test
    fun `works for query not returning a list`() {
        val result =
            kotgresConnectionPool.runSelectQueryReturningList<Boolean>("SELECT true")
        assertEquals(listOf(true), result)
    }

    @Test
    fun `works for one query that has no results`() {
        val result = kotgresConnectionPool.runSelectQueryReturningList<Boolean>(SELECT_QUERY_WITH_NO_RESULTS)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `throws for update query`() {
        assertThrows<KotgresNoResultsException> {
            kotgresConnectionPool.runSelectQueryReturningList<Boolean>(UPDATE_QUERY)
        }
    }

    @Test
    fun `throws for two queries in same line`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningList<Boolean>(TWO_NUMER_QUERIES_IN_ONE_LINE)
        }
    }

    @Test
    fun `throws for two queries in different lines`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningList<Boolean>(TWO_NUMBER_QUERIES_IN_TWO_LINES)
        }
    }

    @Test
    fun `throws for incorrect query`() {
        val error = assertThrows<PSQLException> {
            kotgresConnectionPool.runSelectQueryReturningList<Boolean>(INCORRECT_QUERY)
        }
        assertTrue(error.message!!.contains("syntax error at or near \"SALACT\""))
    }

}