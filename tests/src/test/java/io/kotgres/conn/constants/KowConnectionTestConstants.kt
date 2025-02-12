package io.kotgres.conn.constants

const val INCORRECT_QUERY = "SALACT 123"

const val TWO_NUMER_QUERIES_IN_ONE_LINE = "SELECT 1+1; SELECT 2+2;"
val TWO_NUMBER_QUERIES_IN_TWO_LINES = """
            SELECT 1+1;
            SELECT 2+2;
""".trimIndent()

const val SELECT_QUERY_WITH_NO_RESULTS = "SELECT 1 AS example_column WHERE 1 = 0;"
const val SELECT_MULTIPLE_NUMBERS_QUERY = "SELECT * FROM (VALUES (1), (2), (3), (4), (5)) AS t(num);"

const val UPDATE_QUERY = "UPDATE users SET age = null WHERE age = -1"