package io.kotgres.orm.internal.utils

import io.kotgres.orm.exceptions.query.KotgresNoResultsException
import io.kotgres.orm.exceptions.query.KotgresQueryMultipleResultsException
import io.kotgres.orm.exceptions.query.KotgresUnexpectedResultsException
import io.kotgres.orm.exceptions.query.PSQL_MULTIPLE_RESULTSETS_ERROR_MSG
import io.kotgres.orm.exceptions.query.PSQL_NO_RESULTS_ERROR_MSG
import io.kotgres.orm.exceptions.query.PSQL_UNEXPECTED_RESULTS_ERROR_MSG
import org.postgresql.util.PSQLException

internal object QueryUtils {
    fun handleSelectQueryExceptions(e: PSQLException): Exception {
        return when (e.message) {
            PSQL_NO_RESULTS_ERROR_MSG -> KotgresNoResultsException(e)
            PSQL_MULTIPLE_RESULTSETS_ERROR_MSG -> KotgresQueryMultipleResultsException()
            else -> e
        }
    }

    fun handleUpdateQueryExceptions(e: PSQLException): Exception {
        if (e.message == PSQL_UNEXPECTED_RESULTS_ERROR_MSG) {
            return KotgresUnexpectedResultsException(e)
        }
        return e
    }
}