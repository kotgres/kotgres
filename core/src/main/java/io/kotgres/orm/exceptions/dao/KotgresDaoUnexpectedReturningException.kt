package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresDaoUnexpectedReturningException :
    KotgresException("Passed query has a returning statement which will be ignored, please remove it.")
