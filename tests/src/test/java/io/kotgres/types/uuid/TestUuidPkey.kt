package io.kotgres.types.uuid

import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.uuid.classes.UserWithUuidPkey
import io.kotgres.utils.KotgresTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertNotEquals

const val USER_WITH_UUID_PKEY_TABLE = "users_with_uuid_pkey"

class TestUUID : KotgresTest() {

    private val userWithUuidPkeyDao: PrimaryKeyDao<UserWithUuidPkey, UUID> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `can get insert and get new UUID`() {
        val generatedUUID = UUID.randomUUID()
        val user = UserWithUuidPkey(generatedUUID)
        val insertedUser = userWithUuidPkeyDao.insert(user)
        assertNotEquals(generatedUUID, insertedUser.uuid)
    }
}
