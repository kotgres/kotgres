package io.kotgres.types.json.jsonjava

import io.kotgres.orm.dao.JsonAsJavaJsonDao
import io.kotgres.orm.dao.JsonBinaryAsJavaJsonDao
import io.kotgres.utils.KotgresTest
import org.json.JSONObject
import org.junit.jupiter.api.Nested
import kotlin.test.Test
import kotlin.test.assertEquals




class TestJsonJava : KotgresTest() {

    private val dao by lazy { JsonAsJavaJsonDao(kotgresConnectionPool) }
    private val daoBinary by lazy { JsonBinaryAsJavaJsonDao(kotgresConnectionPool) }
    private val emptyJson = JSONObject()
    private val sampleJson = JSONObject("{\"city\":\"chicago\",\"name\":\"jon doe\",\"age\":\"22\"}")

    @Nested
    inner class json {
        @Test
        fun `can insert`() {
            val toInsert = JsonAsJavaJson(-1, emptyJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonAsJavaJson(-1, sampleJson)
            dao.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonAsJavaJson(-1, sampleJson)
            val inserted = dao.insert(toInsert)

            assertEquals(sampleJson.toString(), inserted.content.toString())
        }
    }

    @Nested
    inner class jsonb {
        @Test
        fun `can insert`() {
            val toInsert = JsonBinaryAsJavaJson(-1, emptyJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert with data`() {
            val toInsert = JsonBinaryAsJavaJson(-1, sampleJson)
            daoBinary.insert(toInsert)
        }

        @Test
        fun `can insert and retrieve`() {
            val toInsert = JsonBinaryAsJavaJson(-1, sampleJson)
            val inserted = daoBinary.insert(toInsert)

            assertEquals(sampleJson.toString(), inserted.content.toString())
        }
    }
}