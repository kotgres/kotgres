package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresUnexpectedGetByUniqueResultsException :
    KotgresException("getByUniqueColumn got more than one entity as a result, but there should only be one.\nIs the column name you passed actually UNIQUE in the Postgres schema?")
