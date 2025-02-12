package io.kotgres.classes.vehicle

import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table

@Table(name = "vehicle_with_enum")
data class VehicleNoEnumInCode(
    @PrimaryKey
    @Generated
    val id: Int,
    val type: String,
)
