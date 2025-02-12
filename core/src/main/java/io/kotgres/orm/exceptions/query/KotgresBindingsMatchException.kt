package io.kotgres.orm.exceptions.query

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresBindingsMatchException(numberOfBindingsInQuery: Int, numberOfBindingsPassed: Int) : KotgresException(
    "Bindings in query and passed bindings count do not match " +
            "($numberOfBindingsInQuery vs $numberOfBindingsPassed)",
)