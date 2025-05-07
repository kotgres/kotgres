package io.kotgres.dao.primarykey.general

import io.kotgres.classes.vehicle.VehicleFullEnum
import io.kotgres.classes.vehicle.VehicleNoEnumInCode
import io.kotgres.classes.vehicle.VehicleNoEnumInCodeWithEnumAnnotation
import io.kotgres.classes.vehicle.VehicleNoEnumInDb
import io.kotgres.classes.vehicle.VehicleNoEnums
import io.kotgres.classes.vehicle.VehicleType
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import org.postgresql.util.PSQLException
import kotlin.test.Test
import kotlin.test.assertEquals

class EnumsTest : KotgresTest() {

    private val vehicleFullEnumDao: PrimaryKeyDao<VehicleFullEnum, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    private val vehicleNoEnumInDbDao: PrimaryKeyDao<VehicleNoEnumInDb, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    private val vehicleNoEnumInCodeDao: PrimaryKeyDao<VehicleNoEnumInCode, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    private val vehicleNoEnumInCodeWithEnumAnnotationDao: PrimaryKeyDao<VehicleNoEnumInCodeWithEnumAnnotation, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    private val vehicleNoEnumsDao: PrimaryKeyDao<VehicleNoEnums, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    @Test
    fun `can fetch and update enum when both in code and db`() {
        val vehicle = VehicleFullEnum(-1, VehicleType.CAR)
        val inserted = vehicleFullEnumDao.insert(vehicle)
        assertEquals(VehicleType.CAR, inserted.type)

        val updated = vehicleFullEnumDao.update(inserted.copy(type = VehicleType.MOTORBIKE))!!
        assertEquals(VehicleType.MOTORBIKE, updated.type)
    }

    @Test
    fun `can fetch and update enum when in code but not on db`() {
        val vehicle = VehicleNoEnumInDb(-1, VehicleType.CAR)
        val inserted = vehicleNoEnumInDbDao.insert(vehicle)
        assertEquals(VehicleType.CAR, inserted.type)

        val updated = vehicleNoEnumInDbDao.update(inserted.copy(type = VehicleType.MOTORBIKE))!!
        assertEquals(VehicleType.MOTORBIKE, updated.type)
    }

    @Test
    fun `can not fetch and update enum when not in code but is in db`() {
        val vehicle = VehicleNoEnumInCode(-1, "car")

        val error = assertThrows<PSQLException> {
            vehicleNoEnumInCodeDao.insert(vehicle)
        }
        Assertions.assertTrue(
            error.message!!.contains(
                "ERROR: column \"type\" is of type vehicle_type but expression is of type character varying",
            ),
        )
    }

    @Test
    fun `can fetch and update enum when not in code but is in db if using @Enum annotation`() {
        val vehicle = VehicleNoEnumInCodeWithEnumAnnotation(-1, "car")
        val inserted = vehicleNoEnumInCodeWithEnumAnnotationDao.insert(vehicle)
        assertEquals(VehicleType.CAR.name.lowercase(), inserted.type)

        val updated = vehicleNoEnumInCodeWithEnumAnnotationDao.update(inserted.copy(type = VehicleType.MOTORBIKE.name.lowercase()))!!
        assertEquals(VehicleType.MOTORBIKE.name.lowercase(), updated.type)
    }

    @Test
    fun `can fetch and update enum when not in code or db`() {
        val vehicle = VehicleNoEnums(-1, "car")
        val inserted = vehicleNoEnumsDao.insert(vehicle)
        assertEquals(VehicleType.CAR.name.lowercase(), inserted.type)

        val updated = vehicleNoEnumsDao.update(inserted.copy(type = VehicleType.MOTORBIKE.name.lowercase()))!!
        assertEquals(VehicleType.MOTORBIKE.name.lowercase(), updated.type)
    }

    // TODO test updating enum value in all cases
    // TODO allow user to use @Enum to tell us how to store enum
    // TODO implement other combinations of enums (db vs code enums)
}
