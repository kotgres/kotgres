package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException
import org.postgresql.util.PSQLException

class KotgresColumnNotFoundInQueryResultException(val psqlException: PSQLException, val columnName: String) :
    KotgresException("The column with name $columnName was not found in the query result.\nYour entity expects it, but the query result does not have it.\nIf you are running a raw query using a DAO make sure that you are selecting * and that you are selecting from the table from the DAO's entity.")
