package io.kotgres.orm.exceptions.query

import io.kotgres.orm.exceptions.base.KotgresException
import org.postgresql.util.PSQLException

const val PSQL_NO_RESULTS_ERROR_MSG = "No results were returned by the query."

class KotgresNoResultsException(val psqlException: PSQLException?) :
    KotgresException("No results were returned by the query.\nPerhaps you meant to run an insert, update or delete instead of a select query? (i.e. you used runSelect for an UPDATE query instead of runUpdate)")
