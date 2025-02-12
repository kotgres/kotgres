package io.kotgres.orm.dao.model.onconflict.dsl.final

import io.kotgres.dsl.ConflictSet
import io.kotgres.orm.dao.model.onconflict.dsl.OnConflictResolution

class OnConflictUpdate(
    target: String,
    val columnToExpressionMap: List<ConflictSet>,
) : OnConflictResolution(target)