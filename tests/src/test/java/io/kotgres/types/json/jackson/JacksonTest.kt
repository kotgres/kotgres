package io.kotgres.types.json.jackson

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotgres.orm.generated.dao.JsonAsJacksonDao
import io.kotgres.orm.generated.dao.JsonBinaryAsJacksonDao
import io.kotgres.utils.KotgresTest
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals


class JacksonTest : KotgresTest() {

    private val dao by lazy { JsonAsJacksonDao(kotgresConnectionPool) }
    private val daoBinary by lazy { JsonBinaryAsJacksonDao(kotgresConnectionPool) }
    private val emptyJson = ObjectMapper().readTree("{}")
    private val sampleJson = ObjectMapper().readTree("{\"name\":\"John\", \"age\":30}")

    @Nested
    inner class json {
        @Test
        fun `can insert`() {
            val toInsert = JsonAsJackson(-1, emptyJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonAsJackson(-1, sampleJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonAsJackson(-1, sampleJson)
            val inserted = dao.insert(toInsert)

            assertEquals(sampleJson, inserted.content)
        }
    }

    @Nested
    inner class jsonb {
        @Test
        fun `can insert`() {
            val toInsert = JsonBinaryAsJackson(-1, emptyJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonBinaryAsJackson(-1, sampleJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonBinaryAsJackson(-1, sampleJson)
            val inserted = daoBinary.insert(toInsert)

            assertEquals(sampleJson, inserted.content)
        }
    }
}