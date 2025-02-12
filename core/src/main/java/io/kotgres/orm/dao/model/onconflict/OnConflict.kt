package io.kotgres.orm.dao.model.onconflict

import io.kotgres.orm.dao.model.onconflict.dsl.OnConflictResolutionBuilder


open class OnConflict(val target: String) {

    companion object {

        fun column(columnName: String): OnConflictResolutionBuilder {
            return OnConflictResolutionBuilder("($columnName)")
        }

        fun columnList(columnNameList: List<String>): OnConflictResolutionBuilder {
            return OnConflictResolutionBuilder("(" + columnNameList.joinToString(",") + ")")
        }

        fun constraint(constraintName: String): OnConflictResolutionBuilder {
            return OnConflictResolutionBuilder("ON CONSTRAINT $constraintName")
        }

    }

}
