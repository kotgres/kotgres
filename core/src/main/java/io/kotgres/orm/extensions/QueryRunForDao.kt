package io.kotgres.orm.extensions

import io.kotgres.dsl.queries.delete.base.DeleteQuery
import io.kotgres.dsl.queries.insert.base.InsertQuery
import io.kotgres.dsl.queries.select.base.SelectQuery
import io.kotgres.dsl.queries.update.base.UpdateQuery
import io.kotgres.orm.dao.AbstractDao

fun <E> SelectQuery.run(dao: AbstractDao<E>): List<E> {
    return dao.runSelect(this)
}

fun <E> InsertQuery.run(dao: AbstractDao<E>): Int {
    return dao.runInsert(this)
}

fun <E> UpdateQuery.run(dao: AbstractDao<E>): Int {
    return dao.runUpdate(this)
}

fun <E> DeleteQuery.run(dao: AbstractDao<E>): Int {
    return dao.runDelete(this)
}
