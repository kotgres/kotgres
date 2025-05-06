package io.kotgres.classes.dog

import io.kotgres.orm.annotations.Table
import java.time.LocalDateTime

@Table(name = "users")
class UserInMainSourceSetNoPrimaryKey(
    val age: Int?,
    val name: String?,
    val dateCreated: LocalDateTime?,
)
