package io.kotgres.types.uuid.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.uuid.USER_WITH_GENERATED_UUID_TABLE
import java.util.UUID

@Table(name = USER_WITH_GENERATED_UUID_TABLE)
class UserWithGeneratedUUID(
    @PrimaryKey
    @Generated(allowUpdates = false)
    val id: Int,
    @Generated
    var uuid: UUID?,
)
