package io.kotgres.orm.exceptions.query

import io.kotgres.orm.exceptions.base.KotgresException

const val PSQL_MULTIPLE_RESULTSETS_ERROR_MSG = "Multiple ResultSets were returned by the query."

class KotgresQueryMultipleResultsException :
    KotgresException("Your query returned multiple result sets.\n" + "" +
            "This is likely because you are running multiple SQL statements at once (i.e. SELECT <...>; SELECT <...>;)\n" +
    "This behaviour is not supported. To fix this, either: run the two statements in two different queries")
