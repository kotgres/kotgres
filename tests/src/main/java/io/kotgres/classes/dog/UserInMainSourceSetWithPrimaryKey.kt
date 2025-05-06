package io.kotgres.classes.dog

import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import java.time.LocalDateTime

@Table(name = "users")
class UserInMainSourceSetWithPrimaryKey(
    val age: Int?,
    @PrimaryKey
    val name: String,
    val dateCreated: LocalDateTime?,
)
