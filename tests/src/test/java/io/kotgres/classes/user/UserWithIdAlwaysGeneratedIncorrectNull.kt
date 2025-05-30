package io.kotgres.classes.user

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table

@Table(name = "users_with_id_always_generated")
data class UserWithIdAlwaysGeneratedIncorrectNull(
    @Generated
    @PrimaryKey
    val id: Int,
    val name: String?
)
