package io.kotgres.orm.internal.processors.model

import io.kotgres.orm.types.base.AbstractMapper
import kotlin.reflect.KClass

internal sealed class MapperInfo {
    data class BuiltInMapperInfo(val kClass: KClass<out AbstractMapper<*>>) : MapperInfo()
    data class CustomMapperInfo(val className: String) : MapperInfo()
}
