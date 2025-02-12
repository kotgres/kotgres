package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresUnexpectedNullException : KotgresException("Got null value but field is not nullable")
