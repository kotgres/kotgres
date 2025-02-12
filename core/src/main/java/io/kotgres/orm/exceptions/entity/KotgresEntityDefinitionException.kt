package io.kotgres.orm.exceptions.entity

import io.kotgres.orm.exceptions.base.KotgresException

/**
 * This error happens when there is an error on the definition of a class
 */
class KotgresEntityDefinitionException(msg: String) :
    KotgresException(msg)
