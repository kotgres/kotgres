package io.kotgres.orm.annotations

import io.kotgres.orm.types.base.AbstractMapper
import kotlin.reflect.KClass


annotation class CustomMapper(
    val mapperClass: KClass<out AbstractMapper<*>>,
)
