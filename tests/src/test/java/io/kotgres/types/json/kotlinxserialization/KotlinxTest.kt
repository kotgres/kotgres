package io.kotgres.types.json.kotlinxserialization

import io.kotgres.orm.generated.dao.JsonAsKotlinxDao
import io.kotgres.orm.generated.dao.JsonBinaryAsKotlinxDao
import io.kotgres.utils.KotgresTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals



class KotlinxTest : KotgresTest() {

    private val dao by lazy { JsonAsKotlinxDao(kotgresConnectionPool) }
    private val daoBinary by lazy { JsonBinaryAsKotlinxDao(kotgresConnectionPool) }
    private val emptyJson = Json.decodeFromString<JsonObject>("{}")
    private val sampleJson = Json.decodeFromString<JsonObject>(
        "{\n" +
                "  \"name\": \"kotlinx.serialization\",\n" +
                "  \"language\": \"Kotlin\"\n" +
                "}"
    )

    @Nested
    inner class json {
        @Test
        fun `can insert`() {
            val toInsert = JsonAsKotlinx(-1, emptyJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonAsKotlinx(-1, sampleJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonAsKotlinx(-1, sampleJson)
            val inserted = dao.insert(toInsert)

            assertEquals(sampleJson.toString(), inserted.content.toString())
        }
    }

    @Nested
    inner class jsonb {
        @Test
        fun `can insert`() {
            val toInsert = JsonBinaryAsKotlinx(-1, emptyJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonBinaryAsKotlinx(-1, sampleJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonBinaryAsKotlinx(-1, sampleJson)
            val inserted = daoBinary.insert(toInsert)

            assertEquals(sampleJson.toString(), inserted.content.toString())
        }
    }
}