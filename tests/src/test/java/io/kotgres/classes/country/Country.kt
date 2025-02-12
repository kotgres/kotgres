package io.kotgres.classes.country

import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.annotations.Unique
import java.time.LocalDateTime

@Table(name = "countries")
class Country(
    @PrimaryKey
    val code: String,
    @Unique
    val fullName: String,
    val dateCreated: LocalDateTime,
)
