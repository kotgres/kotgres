package io.kotgres.classes.user

import io.kotgres.orm.annotations.Table
import java.time.LocalDateTime

@Table(name = "users")
class User(
    val age: Int?,
    val name: String?,
    val dateCreated: LocalDateTime?,
)
