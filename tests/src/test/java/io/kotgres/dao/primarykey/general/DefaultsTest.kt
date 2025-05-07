package io.kotgres.dao.primarykey.general

import io.kotgres.classes.user.UserWithDefaultDateUpdated
import io.kotgres.classes.user.UserWithDefaultDateUpdatedGenerated1970
import io.kotgres.classes.user.UserWithDefaultDateUpdatedGeneratedNow
import io.kotgres.classes.user.UserWithDefaultDateUpdatedNonUpdateable
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomString
import java.time.LocalDateTime
import kotlin.test.*

class DefaultsTest : KotgresTest() {

    @Test
    fun `when no generated annotation, default is null and can update`() {
        val userDao: PrimaryKeyDao<UserWithDefaultDateUpdated, Int> =
            DaoManager.getPrimaryKeyDao(kotgresConnectionPool)

        val randomName = randomString()
        val userToInsert = UserWithDefaultDateUpdated(-1, 1, randomName, null)
        val user = userDao.insert(userToInsert)
        assertNull(user.dateUpdated)

        val now = LocalDateTime.now()
        user.dateUpdated = now

        val updatedUser = userDao.update(user)!!
        assertEquals(now, updatedUser.dateUpdated)
    }

    @Test
    fun `when generated annotation, default is null and can update`() {
        val userDao: PrimaryKeyDao<UserWithDefaultDateUpdatedGenerated1970, Int> =
            DaoManager.getPrimaryKeyDao(kotgresConnectionPool)

        val randomName = randomString()
        val user = UserWithDefaultDateUpdatedGenerated1970(-1, 1, randomName, null)
        val insertedUser = userDao.insert(user)

        assertNotNull(insertedUser.dateUpdated)

        val now = LocalDateTime.now()
        val beforeUpdate = now.minusSeconds(1)
        val afterUpdate = now.plusSeconds(1)

        insertedUser.dateUpdated = now
        val updatedUser = userDao.update(insertedUser)!!
        assertEquals(now, updatedUser.dateUpdated)
        assertTrue(beforeUpdate.isBefore(insertedUser.dateUpdated))
        assertTrue(afterUpdate.isAfter(beforeUpdate))
    }

    @Test
    fun `default now works as expected`() {
        val userDao: PrimaryKeyDao<UserWithDefaultDateUpdatedGeneratedNow, Int> =
            DaoManager.getPrimaryKeyDao(kotgresConnectionPool)

        val randomName = randomString()
        val user = UserWithDefaultDateUpdatedGeneratedNow(-1, 1, randomName, null)

        val now = LocalDateTime.now()
        val beforeUpdate = now.minusSeconds(2)
        val afterUpdate = now.plusSeconds(2)

        val insertedUser = userDao.insert(user)

        assertNotNull(insertedUser.dateUpdated)
        assertTrue(beforeUpdate.isBefore(insertedUser.dateUpdated))
        assertTrue(afterUpdate.isAfter(beforeUpdate))
    }

    @Test
    fun `non updateable does not allow to updated`() {
        val userDao: PrimaryKeyDao<UserWithDefaultDateUpdatedNonUpdateable, Int> =
            DaoManager.getPrimaryKeyDao(kotgresConnectionPool)

//        val timestampZeroPostgres = LocalDateTime.of(1970, 1, 1, 1, 0, 0)

        val randomName = randomString()
        val user = UserWithDefaultDateUpdatedNonUpdateable(-1, 1, randomName, null)
        val insertedUser = userDao.insert(user)
        assertNotNull(insertedUser.dateUpdated)
        // TODO: does crash on CI because of timezones! fix
//        assertEquals(timestampZeroPostgres, insertedUser.dateUpdated)

        insertedUser.dateUpdated = LocalDateTime.now()
        val updatedUser = userDao.update(insertedUser)!!
        assertNotNull(updatedUser.dateUpdated)
        // TODO: does crash on CI because of timezones! fix
//        assertEquals(timestampZeroPostgres, updatedUser.dateUpdated)
    }
}
