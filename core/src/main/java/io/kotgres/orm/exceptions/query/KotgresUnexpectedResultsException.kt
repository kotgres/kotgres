package io.kotgres.orm.exceptions.query

import io.kotgres.orm.exceptions.base.KotgresException
import org.postgresql.util.PSQLException

const val PSQL_UNEXPECTED_RESULTS_ERROR_MSG = "A result was returned when none was expected."

class KotgresUnexpectedResultsException(val psqlException: PSQLException?) :
    KotgresException("Results were returned by the query when none was expected.\nPerhaps you meant to run a select instead of an insert, update or delete query? (i.e. you used runUpdate for a SELECT query instead of runSelect)")
