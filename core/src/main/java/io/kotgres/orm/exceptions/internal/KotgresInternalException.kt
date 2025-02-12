package io.kotgres.orm.exceptions.internal

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresInternalException(msg: String) : KotgresException("$msg. This should not happen. Please report this error through Github issues.")
