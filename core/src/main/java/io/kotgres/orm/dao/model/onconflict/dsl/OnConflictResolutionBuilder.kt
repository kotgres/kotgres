package io.kotgres.orm.dao.model.onconflict.dsl

import io.kotgres.dsl.ConflictSet
import io.kotgres.orm.dao.model.onconflict.dsl.final.OnConflictIgnore
import io.kotgres.orm.dao.model.onconflict.dsl.final.OnConflictUpdate

open class OnConflictResolutionBuilder(private val target: String) {
    fun ignore(): OnConflictIgnore {
        return OnConflictIgnore(target)
    }

    fun update(columnToExpressionMap: List<ConflictSet>): OnConflictUpdate {
        return OnConflictUpdate(target, columnToExpressionMap)
    }
}

