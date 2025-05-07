package io.kotgres.types.timestamp

import io.kotgres.dsl.operators.lessEq
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.manager.DaoManager
import io.kotgres.types.timestamp.classes.TableWithTimestampAsDate
import io.kotgres.types.timestamp.classes.TableWithTimestampAsLocalDateTime
import io.kotgres.types.timestamp.utils.toDate
import io.kotgres.utils.KotgresTest
import java.time.LocalDateTime
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

const val TEST_TIMESTAMP_TABLE = "table_with_timestamp"


class TimestampTest : KotgresTest() {

    private val daoAsLocalDatetime: PrimaryKeyDao<TableWithTimestampAsLocalDateTime, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    private val daoAsDate: PrimaryKeyDao<TableWithTimestampAsDate, Int> by lazy {
        DaoManager.getPrimaryKeyDao(kotgresConnectionPool)
    }

    private val exampleDate = LocalDateTime.of(2000, 1, 2, 6, 1, 5)
    private val currentDate = LocalDateTime.now()

    @Test
    fun `for LocalDateTime mapping works correctly for time 0`() {
        val result =
            daoAsLocalDatetime.insert(TableWithTimestampAsLocalDateTime(-1, exampleDate))
        assertTrue(result.id > 0)
        assertEquals(exampleDate, result.date)
    }

    @Test
    fun `for LocalDateTime mapping works correctly for current time`() {
        val result =
            daoAsLocalDatetime.insert(TableWithTimestampAsLocalDateTime(-1, currentDate))
        assertTrue(result.id > 0)
        assertEquals(currentDate, result.date)
    }

    @Test
    fun `for java_util_Date mapping works correctly for time 0`() {
        val result =
            daoAsDate.insert(TableWithTimestampAsDate(-1, exampleDate.toDate()))
        assertTrue(result.id > 0)
        assertEquals(exampleDate.toDate(), result.date)
    }

    @Test
    fun `for java_util_Date mapping works correctly for current time`() {
        val result =
            daoAsDate.insert(TableWithTimestampAsDate(-1, currentDate.toDate()))
        assertTrue(result.id > 0)
        assertEquals(currentDate.toDate(), result.date)
    }

    @Test
    fun `can filter easily by LocalDateTime`() {
        val result =
            daoAsDate.insert(TableWithTimestampAsDate(-1, currentDate.toDate()))
        assertTrue(result.id > 0)

        Thread.sleep(1_000)

        val rows = daoAsDate.runSelect(
            daoAsDate
                .selectQuery()
                .where("date" lessEq LocalDateTime.now())
        )
        assertTrue(rows.isNotEmpty())
    }

    @Test
    fun `can filter easily by Date`() {
        val result =
            daoAsDate.insert(TableWithTimestampAsDate(-1, currentDate.toDate()))
        assertTrue(result.id > 0)

        Thread.sleep(1_000)

        val rows = daoAsDate.runSelect(
            daoAsDate
                .selectQuery()
                .where("date" lessEq Date())
        )
        assertTrue(rows.isNotEmpty())
    }
}
