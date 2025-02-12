package io.kotgres.classes.user

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import java.time.LocalDateTime

@Table(name = "users_with_default_date_updated")
class UserWithDefaultDateUpdatedGenerated1970(
    @PrimaryKey
    @Generated(allowUpdates = false)
    val id: Int,
    val age: Int?,
    val name: String?,
    @Generated
    var dateUpdated: LocalDateTime?,
)
