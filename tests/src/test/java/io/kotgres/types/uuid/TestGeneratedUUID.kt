package io.kotgres.types.uuid

import io.kotgres.orm.generated.dao.UserWithGeneratedUUIDDao
import io.kotgres.types.uuid.classes.UserWithGeneratedUUID
import io.kotgres.utils.KotgresTest
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

const val USER_WITH_GENERATED_UUID_TABLE = "users_with_generated_uuid"

class TestGeneratedUUID : KotgresTest() {

    private lateinit var userWithUUIDDao: UserWithGeneratedUUIDDao

    override fun setUpTest() {
        userWithUUIDDao = UserWithGeneratedUUIDDao(kotgresConnectionPool)
    }

    @Test
    fun `can get and update UUID`() {
        val user = UserWithGeneratedUUID(-1, null)
        val newUser = userWithUUIDDao.insert(user)
        assertNotNull(newUser.uuid)

        val newUUID = UUID.randomUUID()
        newUser.uuid = newUUID
        val savedUser = userWithUUIDDao.update(newUser)!!
        assertEquals(newUUID, savedUser.uuid)
    }

    // TODO this test indirectly test what happens when the entity has no insert fields, add a separate one for that
    @Test
    fun `can insert void with UUID`() {
        val user = UserWithGeneratedUUID(-1, null)
        userWithUUIDDao.insertVoid(user)
    }

    @Test
    fun `can insert insert returning ID with UUID`() {
        val user = UserWithGeneratedUUID(-1, null)
        userWithUUIDDao.insertReturningId(user)
    }

    @Test
    fun `can insert insert returning void with UUID`() {
        val user = UserWithGeneratedUUID(-1, null)
        userWithUUIDDao.insertVoid(user)
    }

    @Test
    fun `can insert insert list returning void with UUID`() {
        val user1 = UserWithGeneratedUUID(-1, null)
        val user2 = UserWithGeneratedUUID(-1, null)
        userWithUUIDDao.insertVoid(listOf(user1, user2))
    }

    @Test
    fun `can insert insert list returning id with UUID`() {
        val user1 = UserWithGeneratedUUID(-1, null)
        val user2 = UserWithGeneratedUUID(-1, null)
        val result = userWithUUIDDao.insertListReturningId(listOf(user1, user2))
        assertEquals(2, result.size)
    }
}
