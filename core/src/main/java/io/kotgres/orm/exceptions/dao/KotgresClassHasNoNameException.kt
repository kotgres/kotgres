package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresClassHasNoNameException(platform: String) :
    KotgresException("Passed $platform class has no name and it is needed for this feature.\n Anonymous classes or similar are not supported, use a regular class.")
