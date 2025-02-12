package io.kotgres.orm.exceptions.entity

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresNoEmptyConstructorException(entityName: String) :
    KotgresException("$entityName has no empty constructor.\nEither create one manually or add 'plugin-noarg' to Gradle for the annotation io.kotgres.orm.annotations.Table (more info in the docs)")
