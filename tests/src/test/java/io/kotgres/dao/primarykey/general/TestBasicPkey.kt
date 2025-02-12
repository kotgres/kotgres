package io.kotgres.dao.primarykey.general

import io.kotgres.classes.country.Country
import io.kotgres.classes.user.UserWithId
import io.kotgres.classes.user.UserWithIdAlwaysGeneratedIncorrectNull
import io.kotgres.dsl.ConflictSet
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.dao.model.onconflict.OnConflict
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomString
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue


class TestBasicPkey : KotgresTest() {

    private val countryDao: PrimaryKeyDao<Country, String> by lazy {
        DaoManager.getPrimaryKeyDao(Country::class, kotgresConnectionPool)
    }
    private val userDao: PrimaryKeyDao<UserWithId, Int> by lazy {
        DaoManager.getPrimaryKeyDao(UserWithId::class, kotgresConnectionPool)
    }

    private val userWithIdAlwaysGeneratedIncorrectNullDao: PrimaryKeyDao<UserWithIdAlwaysGeneratedIncorrectNull, Int> by lazy {
        DaoManager.getPrimaryKeyDao(UserWithIdAlwaysGeneratedIncorrectNull::class, kotgresConnectionPool)
    }

    @Test
    fun `update returns null if entity does not exist`() {
        val user = UserWithId(-1, "mohamed", 53, LocalDateTime.now())
        val updatedUser = userDao.update(user)
        assertNull(updatedUser)
    }

    @Test
    fun `can insert user and update it with PrimaryKeyDao`() {
        val newAge = 1235

        val user = UserWithId(-1, "mohamed", 53, LocalDateTime.now())
        val dbUser = userDao.insert(user)

        val newUser = dbUser.copy(age = newAge)
        userDao.update(newUser)

        val dbNewUser = userDao.getByPrimaryKey(dbUser.id)!!
        assertEquals(dbNewUser.age, newAge)
    }

    @Test
    fun `can insert user and updateVoid it with PrimaryKeyDao`() {
        val newAge = 1235

        val user = UserWithId(-1, "mohamed", 53, LocalDateTime.now())
        val dbUser = userDao.insert(user)

        val newUser = dbUser.copy(age = newAge)
        val rows = userDao.updateVoid(newUser)
        assertEquals(1, rows)

        val dbNewUser = userDao.getByPrimaryKey(dbUser.id)!!
        assertEquals(dbNewUser.age, newAge)
    }

    @Test
    fun `can insert user and updateReturningIds it with PrimaryKeyDao`() {
        val newAge = 1235

        val user = UserWithId(-1, "mohamed", 53, LocalDateTime.now())
        val dbUser = userDao.insert(user)

        val newUser = dbUser.copy(age = newAge)
        val ids = userDao.updateReturningIds(newUser)
        assertEquals(ids, listOf(dbUser.id))

        val dbNewUser = userDao.getByPrimaryKey(dbUser.id)!!
        assertEquals(dbNewUser.age, newAge)
    }

    @Test
    fun `can insert users and get them with getByIdList`() {
        val user = UserWithId(-1, "mohamed", 53, LocalDateTime.now())
        val dbUser1 = userDao.insert(user)
        val user2 = UserWithId(-1, "david", 12, LocalDateTime.now())
        val dbUser2 = userDao.insert(user2)

        val users = userDao.getByPrimaryKeyList(listOf(dbUser1.id, dbUser2.id))
        assertEquals(users.size, 2)
        assertNotNull(users.find { it.id == dbUser1.id })
        assertNotNull(users.find { it.id == dbUser2.id })
    }

    @Test
    fun `can insert user and fetch it by ID with PrimaryKeyDao`() {
        val user = UserWithId(-1, "mohamed", 53, LocalDateTime.now())
        val insertedUser = userDao.insert(user)

        val dbUser = userDao.getByPrimaryKey(insertedUser.id)!!

        assertEquals(insertedUser.id, dbUser.id)
    }

    @Test
    fun `can insert with PrimaryKeyDao that has string as pkey`() {
        val countryId = "FR"
        countryDao.deleteById(countryId)

        val country = Country(countryId, "France", LocalDateTime.now())
        countryDao.insert(country)

        val dbCountry = countryDao.getByPrimaryKey(countryId)!!

        assertEquals(dbCountry.code, countryId)
    }

    @Test
    fun `can insert multiple with PrimaryKeyDao that has string as pkey`() {
        val countryId1 = "FR"
        val countryId2 = "BE"

        countryDao.deleteById(countryId1)
        countryDao.deleteById(countryId2)

        val country1 = Country(countryId1, "France", LocalDateTime.now())
        val country2 = Country(countryId2, "Belgium", LocalDateTime.now())
        countryDao.insert(listOf(country1, country2))

        val dbCountry1 = countryDao.getByPrimaryKey(countryId1)!!
        val dbCountry2 = countryDao.getByPrimaryKey(countryId2)!!

        assertEquals(countryId1, dbCountry1.code)
        assertEquals(countryId2, dbCountry2.code)
    }

    @Test
    fun `on conflict on column and ignore works`() {
        val countryId1 = "FR"

        countryDao.deleteById(countryId1)

        val country1 = Country(countryId1, "France", LocalDateTime.now())
        countryDao.insert(country1)
        countryDao.insert(country1, OnConflict.column("code").ignore())
    }

    @Test
    fun `on conflict on column and ignore works with list`() {
        val countryId1 = "FR"

        countryDao.deleteById(countryId1)

        val country1 = Country(countryId1, "France", LocalDateTime.now())
        countryDao.insert(country1)
        countryDao.insert(listOf(country1), OnConflict.column("code").ignore())
    }

    @Test
    fun `on conflict on column and update works`() {
        val countryId1 = "FR"

        countryDao.deleteById(countryId1)

        val country1 = Country(countryId1, "France", LocalDateTime.now())
        countryDao.insert(country1)
        countryDao.insert(
            country1, OnConflict.column("code").update(
                listOf(
                    ConflictSet("date_created", "now()")
                )
            )
        )
    }

    @Test
    fun `selectQuery works as expected`() {
        val countryId = "FR"

        countryDao.deleteById(countryId)

        val query =
            countryDao
                .selectQuery()
                .where("asd = true")
                .toSql()

        assertEquals("SELECT * FROM countries WHERE asd = true", query)
    }

    @Test
    fun `upsertOnConflictDoNothing works with columns`() {
        val country = Country(randomString(), "hello", LocalDateTime.now())

        val runUpsert = {
            countryDao.upsertOnConflictDoNothing(country, columns = listOf("code"))
        }

        val updatedEntity1 = runUpsert()
        val updatedEntity2 = runUpsert()

        assertNotNull(updatedEntity1)
        assertNull(updatedEntity2)
    }

    @Test
    fun `upsertOnConflictDoNothing works with columns and multiple entities`() {
        val country1 = Country(randomString(), "hello", LocalDateTime.now())
        val country2 = Country(randomString(), "hello", LocalDateTime.now())

        countryDao.upsertOnConflictDoNothing(country1, columns = listOf("code"))
        val updatedEntities =
            countryDao.upsertListOnConflictDoNothing(listOf(country1, country2), columns = listOf("code"))

        assertEquals(1, updatedEntities.size)
        assertEquals(country2.code, updatedEntities.first().code)
    }

    @Test
    fun `upsertOnConflictDoNothing works with constraint`() {
        val country = Country(randomString(), "hello", LocalDateTime.now())

        val runUpsert = {
            countryDao.upsertOnConflictDoNothing(country, constraintName = "countries_pkey")
        }

        val updatedEntity1 = runUpsert()
        val updatedEntity2 = runUpsert()

        assertNotNull(updatedEntity1)
        assertNull(updatedEntity2)
    }

    @Test
    fun `can insert void with UUID`() {
        countryDao.insertVoid(getRandomCountry())
    }

    private fun getRandomCountry() = Country(randomString(10), randomString(), LocalDateTime.now())

    @Test
    fun `can insert insert returning ID with UUID`() {
        countryDao.insertReturningId(getRandomCountry())
    }

    @Test
    fun `can insert insert returning void with UUID`() {
        countryDao.insertVoid(getRandomCountry())
    }

    @Test
    fun `can insert insert list returning void with UUID`() {
        countryDao.insertVoid(listOf(getRandomCountry(), getRandomCountry()))
    }

    @Test
    fun `can insert insert list returning id with UUID (uses DEFAULT VALUES statement)`() {
        val result = countryDao.insertListReturningId(listOf(getRandomCountry(), getRandomCountry()))
        assertEquals(2, result.size)
    }

    @Test
    fun `inserting same primary key twice crashes`() {
        val randomCountry = getRandomCountry()
        countryDao.insert(randomCountry)

        val error = assertThrows<PSQLException> {
            countryDao.insert(randomCountry)
        }
        assertTrue(error.message!!.contains("ERROR: duplicate key value violates unique constraint \"countries_pkey\""))
    }

    @Test
    fun `crashes when inserting not to not-null place`() {
        val entity = UserWithIdAlwaysGeneratedIncorrectNull(-1, null)

        val error = assertThrows<PSQLException> {
            userWithIdAlwaysGeneratedIncorrectNullDao.insert(entity)
        }
        assertTrue(error.message!!.contains("ERROR: null value in column \"name\" of relation \"users_with_id_always_generated\" violates not-null constraint"))
    }
}
