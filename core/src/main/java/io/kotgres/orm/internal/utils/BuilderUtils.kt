package io.kotgres.orm.internal.utils

internal object BuilderUtils {

    private val daoPostfix = "Dao"

    fun entityNameToDaoName(entityName: String): String {
        return "${entityName}${daoPostfix}"
    }

    fun daoNameToEntityName(daoName: String): String {
        return daoName.dropLast(daoPostfix.length)
    }
}