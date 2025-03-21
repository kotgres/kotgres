package io.kotgres.types.custom.classes

import io.kotgres.orm.annotations.*
import java.time.LocalDateTime

@Table(name = "users_with_id")
data class UserWithWrappedIntJava(
    @PrimaryKey
    @Generated
    var id: Int,
    val name: String?,
    @CustomMapper(mapperClass = CustomMapperWrappedIntJava::class)
    var age: WrappedInt?,
    val dateCreated: LocalDateTime?,
)
