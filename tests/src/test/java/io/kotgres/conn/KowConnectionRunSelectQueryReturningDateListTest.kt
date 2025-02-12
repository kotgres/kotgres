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
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val SELECT_MULTIPLE_BOOLEANS_QUERY = "SELECT * FROM (VALUES (now()), (now())) AS t(d);"


class KotgresConnectionrunSelectQueryReturningDateListTest : KotgresTest() {


    @Test
    fun `works for expected query and returns expected result`() {
        val result =
            kotgresConnectionPool.runSelectQueryReturningList<Date>(SELECT_MULTIPLE_BOOLEANS_QUERY)
        assertEquals(2, result.size)
    }

    @Test
    fun `works for query not returning a list`() {
        val result =
            kotgresConnectionPool.runSelectQueryReturningList<Date>("SELECT now()")
        assertEquals(1, result.size)
    }

    @Test
    fun `works for one query that has no results`() {
        val result = kotgresConnectionPool.runSelectQueryReturningList<Date>(SELECT_QUERY_WITH_NO_RESULTS)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `throws for update query`() {
        assertThrows<KotgresNoResultsException> {
            kotgresConnectionPool.runSelectQueryReturningList<Date>(UPDATE_QUERY)
        }
    }

    @Test
    fun `throws for two queries in same line`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningList<Date>(TWO_NUMER_QUERIES_IN_ONE_LINE)
        }
    }

    @Test
    fun `throws for two queries in different lines`() {
        assertThrows<KotgresQueryMultipleResultsException> {
            kotgresConnectionPool.runSelectQueryReturningList<Date>(TWO_NUMBER_QUERIES_IN_TWO_LINES)
        }
    }

    @Test
    fun `throws for incorrect query`() {
        val error = assertThrows<PSQLException> {
            kotgresConnectionPool.runSelectQueryReturningList<Date>(INCORRECT_QUERY)
        }
        assertTrue(error.message!!.contains("syntax error at or near \"SALACT\""))
    }

}