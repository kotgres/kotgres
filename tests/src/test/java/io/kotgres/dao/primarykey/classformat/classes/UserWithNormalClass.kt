package io.kotgres.dao.primarykey.classformat.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import java.time.LocalDateTime

// class to see if it also works (most tests are with data class)
@Table(name = "users_with_id")
class UserWithNormalClass(
    @PrimaryKey
    @Generated
    var id: Int,
    val name: String?,
    var age: Int?,
    val dateCreated: LocalDateTime?,
)
