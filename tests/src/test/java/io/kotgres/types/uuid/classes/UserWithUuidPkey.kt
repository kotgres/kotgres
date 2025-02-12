package io.kotgres.types.uuid.classes

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.types.uuid.USER_WITH_UUID_PKEY_TABLE
import java.util.UUID

@Table(name = USER_WITH_UUID_PKEY_TABLE)
class UserWithUuidPkey(
    @PrimaryKey
    @Generated(allowUpdates = false)
    var uuid: UUID,
)
