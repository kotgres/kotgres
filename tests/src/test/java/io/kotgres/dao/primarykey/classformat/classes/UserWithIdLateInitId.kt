package io.kotgres.dao.primarykey.classformat.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import java.time.LocalDateTime

@Table(name = "users_with_id")
data class UserWithIdLateInitId(
    val name: String?,
    var age: Int?,
    val dateCreated: LocalDateTime?,
) {
    @PrimaryKey
    @Generated
    var id: Int = -1
}
