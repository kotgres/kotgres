package io.kotgres.types.custom.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.annotations.CustomMapper
import java.time.LocalDateTime

@Table(name = "users_with_id")
data class UserWithWrappedInt(
    @PrimaryKey
    @Generated
    var id: Int,
    val name: String?,
    @CustomMapper(mapperClass = CustomMapperWrappedInt::class)
    var age: WrappedInt?,
    val dateCreated: LocalDateTime?,
)
