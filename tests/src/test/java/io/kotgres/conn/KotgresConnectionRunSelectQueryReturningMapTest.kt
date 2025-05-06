package io.kotgres.conn

import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue


class KotgresConnectionRunSelectQueryReturningMapTest : KotgresTest() {

    @Test
    fun `works for one query returning different values`() {
        val result =
            kotgresConnectionPool.runSelectQueryReturningMap("SELECT 'hello' as greeting, 'bye' as farewell, 1 as number")
        assertNotNull(result)
        assertEquals("hello", result["greeting"]!!)
        assertEquals("bye", result["farewell"]!!)
        assertEquals("1", result["number"]!!)
    }

    @Test
    fun `works for query returning nothing`() {
        val result = kotgresConnectionPool.runSelectQueryReturningMap("SELECT")
        assertNotNull(result)
        assertTrue(result.isEmpty())
    }

}
