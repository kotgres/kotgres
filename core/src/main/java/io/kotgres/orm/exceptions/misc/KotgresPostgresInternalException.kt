package io.kotgres.orm.exceptions.misc

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresPostgresInternalException(
    msg: String,
) : KotgresException("UNEXPECTED ERROR. Please report this issue | $msg") {
    constructor(e: Exception) : this("Unexpected Postgres internal exception:" + e.message)
}
