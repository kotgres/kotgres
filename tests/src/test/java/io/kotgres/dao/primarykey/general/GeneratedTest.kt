package io.kotgres.dao.primarykey.general

import io.kotgres.classes.user.UserWithIdAlwaysGenerated
import io.kotgres.dsl.insertInto
import io.kotgres.orm.generated.dao.UserWithIdAlwaysGeneratedDao
import io.kotgres.orm.exceptions.query.KotgresCannotInsertToGeneratedAlwaysException
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomNumber
import io.kotgres.utils.randomString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GeneratedTest : KotgresTest() {

    private val userWithIdNoGeneratedDao by lazy { UserWithIdAlwaysGeneratedDao(kotgresConnectionPool) }

    @Test
    fun `error when forgetting @Generated`() {
        assertThrows<KotgresCannotInsertToGeneratedAlwaysException> {
            userWithIdNoGeneratedDao.insert(
                UserWithIdAlwaysGenerated(
                    (1000.toString() + randomNumber(1_000_000)).toInt(),
                    randomString(),
                )
            )
        }
    }

    @Test
    fun `can avoid error when forgetting @Generated by using overridingUserValue`() {
        userWithIdNoGeneratedDao.runInsert(
            insertInto(userWithIdNoGeneratedDao.tableName)
                .columns("id", "name")
                .overridingUserValue()
                .value(listOf(1000.toString() + randomNumber(1_000_000).toInt(), randomString()))
        )
    }

    @Test
    fun `can avoid error when forgetting @Generated by using overridingSystemValue`() {
        userWithIdNoGeneratedDao.runInsert(
            insertInto(userWithIdNoGeneratedDao.tableName)
                .columns("id", "name")
                .overridingSystemValue()
                .value(listOf(1000.toString() + randomNumber(1_000_000).toInt(), randomString()))
        )
    }
}