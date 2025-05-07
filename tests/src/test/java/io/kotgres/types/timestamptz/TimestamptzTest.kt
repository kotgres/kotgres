package io.kotgres.types.timestamptz

import io.kotgres.dsl.operators.lessEq
import io.kotgres.orm.generated.dao.TableWithTimestamptzAsDateDao
import io.kotgres.orm.generated.dao.TableWithTimestamptzAsLocalDateTimeDao
import io.kotgres.orm.generated.dao.TableWithTimestamptzAsOffsetDateTimeDao
import io.kotgres.orm.exceptions.entity.KotgresEntityDefinitionException
import io.kotgres.types.timestamp.utils.toDate
import io.kotgres.types.timestamptz.classes.TableWithTimestamptzAsDate
import io.kotgres.types.timestamptz.classes.TableWithTimestamptzAsLocalDateTime
import io.kotgres.types.timestamptz.classes.TableWithTimestamptzAsOffsetDateTime
import io.kotgres.types.timestamptz.utils.toDate
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.*
import kotlin.test.Test

//
const val TEST_TIMESTAMPTZ_TABLE = "table_with_timestamptz"


class TestTimestamptz : KotgresTest() {
    private lateinit var daoAsLocalDateTime: TableWithTimestamptzAsLocalDateTimeDao
    private lateinit var daoAsOffset: TableWithTimestamptzAsOffsetDateTimeDao
    private lateinit var daoAsDate: TableWithTimestamptzAsDateDao

    private val exampleDate = LocalDateTime.of(2000, 1, 2, 6, 1, 5)
    private val exampleDateOffset = OffsetDateTime.of(exampleDate, ZoneOffset.UTC)
    private val currentDate = LocalDateTime.now()
    private val currentDateOffset = OffsetDateTime.of(currentDate, ZoneOffset.UTC)

    override fun setUpTest() {
        daoAsLocalDateTime = TableWithTimestamptzAsLocalDateTimeDao(kotgresConnectionPool)
        daoAsOffset = TableWithTimestamptzAsOffsetDateTimeDao(kotgresConnectionPool)
        daoAsDate = TableWithTimestamptzAsDateDao(kotgresConnectionPool)
    }

    @Test
    fun `for LocalDateTime it throws `() {
        val exception =
            assertThrows<KotgresEntityDefinitionException> {
                daoAsLocalDateTime.insert(TableWithTimestamptzAsLocalDateTime(-1, exampleDate))
            }
        assertEquals(
            """
                Cannot map TIMESTAMPTZ to java.time.LocalDateTime. To fix this, use java.time.OffsetDateTime instead in your model or create a custom mapper for java.time.LocalDateTime following these docs <link>.
                """.trim(),
            exception.message,
        )
    }

    @Test
    fun `for OffsetDateTime mapping works correctly for time 0`() {
        val result =
            daoAsOffset.insert(TableWithTimestamptzAsOffsetDateTime(-1, exampleDateOffset))
        assertTrue(result.id > 0)
        assertEquals(exampleDateOffset, result.date)
    }

    @Test
    fun `for LocalDateTime mapping works correctly for current time`() {
        val result =
            daoAsOffset.insert(TableWithTimestamptzAsOffsetDateTime(-1, currentDateOffset))
        assertTrue(result.id > 0)
        assertEquals(currentDateOffset, result.date)
    }

    @Test
    fun `for java_util_Date mapping works correctly for time 0`() {
        val result =
            daoAsDate.insert(TableWithTimestamptzAsDate(-1, exampleDateOffset.toDate()))
        assertTrue(result.id > 0)
        assertEquals(exampleDateOffset.toDate(), result.date)
    }

    @Test
    fun `for java_util_Date mapping works correctly for current time`() {
        val result =
            daoAsDate.insert(TableWithTimestamptzAsDate(-1, currentDateOffset.toDate()))
        assertTrue(result.id > 0)
        assertEquals(currentDateOffset.toDate(), result.date)
    }

    @Test
    fun `can filter easily by LocalDateTime`() {
        val result =
            daoAsDate.insert(TableWithTimestamptzAsDate(-1, currentDate.toDate()))
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
            daoAsDate.insert(TableWithTimestamptzAsDate(-1, currentDate.toDate()))
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
