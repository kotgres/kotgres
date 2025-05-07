package io.kotgres.dao.primarykey.general

import io.kotgres.classes.country.CountryWithUnique
import io.kotgres.orm.generated.dao.CountryWithUniqueDao
import io.kotgres.utils.KotgresTest
import io.kotgres.utils.randomString
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class UniqueTest : KotgresTest() {

    @Test
    fun `getBy returns null when no rows found`() {
        val countryDao = CountryWithUniqueDao(kotgresConnectionPool)
        val randomName = randomString()
        val dbCountry = countryDao.getByUniqueColumn("full_name", randomName)
        assertNull(dbCountry)
    }

    @Test
    fun `getBy method is generated for uniques and works`() {
        val countryDao = CountryWithUniqueDao(kotgresConnectionPool)

        val randomCode = randomString()
        val randomName = randomString()
        val country = CountryWithUnique(randomCode, randomName, LocalDateTime.now())
        countryDao.insert(country)

        val dbCountry = countryDao.getByUniqueColumn("full_name", randomName)
        assertEquals(country.fullName, dbCountry!!.fullName)
    }

    @Test
    fun `deleteBy returns false when no rows found`() {
        val countryDao = CountryWithUniqueDao(kotgresConnectionPool)
        val randomName = randomString()
        val deleted = countryDao.deleteByColumnValue("full_name", randomName)
        assertEquals(0, deleted)
    }

    @Test
    fun `deleteBy method is generated for uniques and works`() {
        val countryDao = CountryWithUniqueDao(kotgresConnectionPool)

        val randomCode = randomString()
        val randomName = randomString()
        val country = CountryWithUnique(randomCode, randomName, LocalDateTime.now())
        countryDao.insert(country)

        val deleted = countryDao.deleteByColumnValue("full_name", randomName)
        assertEquals(1, deleted)
    }

    // TODO test crash when using unique what it is not actually unique
}
