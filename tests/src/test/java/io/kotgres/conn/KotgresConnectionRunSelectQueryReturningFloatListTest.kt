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

const val SELECT_MULTIPLE_FLOAT_QUERY = "SELECT * FROM (VALUES (1.1::real), (2.3::real), (3.3::real), (4.5::real), (5.9::real)) AS t(num);"
private val SELECT_MULTIPLE_FLOAT_RESULTS = listOf(1.1f, 2.3f, 3.3f, 4.5f, 5.9f)



class KotgresConnectionRunSelectQueryReturningFloatListTest : KotgresTest() {

        @Test
        fun `works for expected query and returns expected result`() {
            val result =
                kotgresConnectionPool.runSelectQueryReturningList<Float>(SELECT_MULTIPLE_FLOAT_QUERY)
            assertEquals(SELECT_MULTIPLE_FLOAT_RESULTS, result)
        }

        @Test
        fun `works for query not returning a list`() {
            val result =
                kotgresConnectionPool.runSelectQueryReturningList<Float>("SELECT 1.6::real")
            assertEquals(listOf(1.6f), result)
        }

        @Test
        fun `works for one query that has no results`() {
            val result = kotgresConnectionPool.runSelectQueryReturningList<Float>(SELECT_QUERY_WITH_NO_RESULTS)
            assertTrue(result.isEmpty())
        }

        @Test
        fun `throws for update query`() {
            assertThrows<KotgresNoResultsException> {
                kotgresConnectionPool.runSelectQueryReturningList<Float>(UPDATE_QUERY)
            }
        }

        @Test
        fun `throws for two queries in same line`() {
            assertThrows<KotgresQueryMultipleResultsException> {
                kotgresConnectionPool.runSelectQueryReturningList<Float>(TWO_NUMER_QUERIES_IN_ONE_LINE)
            }
        }

        @Test
        fun `throws for two queries in different lines`() {
            assertThrows<KotgresQueryMultipleResultsException> {
                kotgresConnectionPool.runSelectQueryReturningList<Float>(TWO_NUMBER_QUERIES_IN_TWO_LINES)
            }
        }

        @Test
        fun `throws for incorrect query`() {
            val error = assertThrows<PSQLException> {
                kotgresConnectionPool.runSelectQueryReturningList<Float>(INCORRECT_QUERY)
            }
            assertTrue(error.message!!.contains("syntax error at or near \"SALACT\""))
        }

}