package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresColumnNotFoundInEntityException(val columnName: String, availableFields: List<String>) :
    KotgresException(
        "Unknown column $columnName.\nIs that the right name? Known columns : ${
            availableFields.joinToString(
                ", "
            )
        }"
    )
