package io.kotgres.types.json.gson

import com.google.gson.JsonParser
import io.kotgres.orm.dao.JsonAsGsonDao
import io.kotgres.orm.dao.JsonBinaryAsGsonDao
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals


class TestGson : KotgresTest() {

    private val dao by lazy { JsonAsGsonDao(kotgresConnectionPool) }
    private val daoBinary by lazy { JsonBinaryAsGsonDao(kotgresConnectionPool) }
    private val emptyJson = JsonParser.parseString("{}")
    private val sampleJson = JsonParser.parseString(
        ("[{\"dorsal\":6," + "\"name\":\"Iniesta\","
                + "\"demarcation\":[\"Right winger\",\"Midfielder\"],"
                + "\"team\":\"FC Barcelona\"}]")
    )

    @Nested
    inner class json {
        @Test
        fun `can insert`() {
            val toInsert = JsonAsGson(-1, emptyJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonAsGson(-1, sampleJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonAsGson(-1, sampleJson)
            val inserted = dao.insert(toInsert)

            assertEquals(sampleJson, inserted.content)
        }
    }

    @Nested
    inner class jsonb {
        @Test
        fun `can insert`() {
            val toInsert = JsonBinaryAsGson(-1, emptyJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonBinaryAsGson(-1, sampleJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonBinaryAsGson(-1, sampleJson)
            val inserted = daoBinary.insert(toInsert)

            assertEquals(sampleJson, inserted.content)
        }
    }
}