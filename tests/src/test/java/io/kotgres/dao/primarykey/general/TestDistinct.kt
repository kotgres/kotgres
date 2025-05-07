package io.kotgres.dao.primarykey.general

import io.kotgres.classes.user.UserWithId
import io.kotgres.dsl.operators.distinct
import io.kotgres.dsl.select
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import kotlin.test.Test


class TestDistinct : KotgresTest() {

    private val userDao: PrimaryKeyDao<UserWithId, Int> by lazy {
        DaoManager.getPrimaryKeyDao(
            kotgresConnectionPool
        )
    }

    @Test
    fun `can run a distinct using the dao, as long as * is returned`() {
        userDao.runSelect(
            select("distinct age , *")
                .from(userDao.tableName)
        )
    }

    @Test
    fun `can run a distinct using the dao, as long as * is returned (with dsl operator)`() {
        userDao.runSelect(
            select(distinct("age"), "*")
                .from(userDao.tableName)
        )
    }


}
