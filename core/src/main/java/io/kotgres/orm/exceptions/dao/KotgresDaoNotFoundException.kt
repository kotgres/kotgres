package io.kotgres.orm.exceptions.dao

import io.kotgres.orm.exceptions.base.KotgresException

/**
 * This error happens when there is an error on the defition of a class
 */
class KotgresDaoNotFoundException(
    entity: String,
    allDaos: List<String>,
) : KotgresException(
    "Dao for $entity not found.\nCheck the common errors section in the documentation for more help. ${
        if (allDaos.isEmpty()) {
            "No entities found in the project"
        } else {
            "Known entities: ${
                allDaos.joinToString(
                    ",",
                )
            }"
        }
    }."
)
