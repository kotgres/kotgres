package io.kotgres.types.custom.classes

import io.kotgres.orm.types.base.CustomMapper


class CustomMapperWrappedInt : CustomMapper<WrappedInt>(WrappedInt::class) {
    override fun fromSql(string: String): WrappedInt? {
        return WrappedInt(string.toInt())
    }

    override fun toSql(value: WrappedInt): String? {
        return value.int.toString()
    }

    override val postgresTypes: List<String>?
        get() = listOf("integer")
}

