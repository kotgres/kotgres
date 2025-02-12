package io.kotgres.classes.user

import io.kotgres.orm.annotations.Table
import java.time.LocalDateTime

@Table(name = "users")
class UserWithWrongAgeType(
    val age: String?,
    val name: String?,
    val dateCreated: LocalDateTime?,
)
