package io.kotgres.conn

import io.kotgres.conn.constants.*
import io.kotgres.orm.exceptions.query.KotgresNoResultsException
import io.kotgres.orm.exceptions.query.KotgresQueryMultipleResultsException
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private val SELECT_MULTIPLE_INTS_RESULTS = listOf(1, 2, 3, 4, 5)


class KotgresConnectionRunSelectQueryReturningIntListTest : KotgresTest() {


        @Test
        fun `works for expected query and returns expected result`() {
            val result =
                kotgresConnectionPool.runSelectQueryReturningList<Int>(SELECT_MULTIPLE_NUMBERS_QUERY)
            assertEquals(SELECT_MULTIPLE_INTS_RESULTS, result)
        }

        @Test
        fun `works for query not returning a list`() {
            val result =
                kotgresConnectionPool.runSelectQueryReturningList<Int>("SELECT 1")
            assertEquals(listOf(1), result)
        }

        @Test
        fun `works for one query that has no results`() {
            val result = kotgresConnectionPool.runSelectQueryReturningList<Int>(SELECT_QUERY_WITH_NO_RESULTS)
            assertTrue(result.isEmpty())
        }

        @Test
        fun `throws for update query`() {
            assertThrows<KotgresNoResultsException> {
                kotgresConnectionPool.runSelectQueryReturningList<Int>(UPDATE_QUERY)
            }
        }

        @Test
        fun `throws for two queries in same line`() {
            assertThrows<KotgresQueryMultipleResultsException> {
                kotgresConnectionPool.runSelectQueryReturningList<Int>(TWO_NUMER_QUERIES_IN_ONE_LINE)
            }
        }

        @Test
        fun `throws for two queries in different lines`() {
            assertThrows<KotgresQueryMultipleResultsException> {
                kotgresConnectionPool.runSelectQueryReturningList<Int>(TWO_NUMBER_QUERIES_IN_TWO_LINES)
            }
        }

        @Test
        fun `throws for incorrect query`() {
            val error = assertThrows<PSQLException> {
                kotgresConnectionPool.runSelectQueryReturningList<Int>(INCORRECT_QUERY)
            }
            assertTrue(error.message!!.contains("syntax error at or near \"SALACT\""))
        }

}
