package io.kotgres

import io.kotgres.utils.KotgresTest
import kotlin.test.Test
import java.sql.PreparedStatement

class ConnectionTest : KotgresTest() {

    @Test
    fun `connection works and allows running select`() {
        val query = "SELECT * FROM users_with_id WHERE id = ANY(?)"
        kotgresConnectionPool.getConnection().use { connection ->

            val st: PreparedStatement = conn.prepareStatement(query)

            val array = conn.createArrayOf("INT", listOf(1).toTypedArray())
            st.setArray(1, array)

            val resultSet = st.executeQuery()

            st.close()
        }
    }
}