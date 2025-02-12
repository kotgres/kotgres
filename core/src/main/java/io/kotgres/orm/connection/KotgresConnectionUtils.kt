package io.kotgres.orm.connection

import io.kotgres.orm.exceptions.query.KotgresNoColumnsReturnedException
import io.kotgres.orm.exceptions.query.KotgresTooManyColumnsReturnedException
import io.kotgres.orm.exceptions.query.KotgresTooManyRowsReturnedException
import io.kotgres.orm.internal.utils.QueryUtils
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.ResultSet

object KotgresConnectionUtils {

    @Deprecated("Do not use, only public because it's needed for inline functions. Use KotgresConnectionPool.runSelectQueryReturningOne")
    fun <T> runSelectQueryReturningOne(
        query: String,
        processRow: (ResultSet) -> T,
        getConnection: () -> Connection
    ): T? {
        getConnection().use { conn ->
            val st = conn.createStatement()
            st.use {
                val resultSet = try {
                    it.executeQuery(query)
                } catch (e: PSQLException) {
                    throw QueryUtils.handleSelectQueryExceptions(e)
                }

                checkResultSetColumnCount(resultSet)

                val result = if (resultSet.next()) {
                    processRow(resultSet)
                } else {
                    null
                }

                val hasMoreResults = resultSet.next()
                if (hasMoreResults) {
                    throw KotgresTooManyRowsReturnedException()
                }

                return result
            }
        }
    }
    @Deprecated("Do not use, only public because it's needed for inline functions. Use KotgresConnectionPool.runSelectQueryReturningList")
    fun <T : Any> runSelectQueryReturningList(
        string: String, processRow: (ResultSet) -> T,
        getConnection: () -> Connection
    ): List<T> {
        getConnection().use { conn ->
            val st = conn.createStatement()
            st.use {
                val returnList = mutableListOf<T>()
                val resultSet = try {
                    it.executeQuery(string)
                } catch (e: PSQLException) {
                    throw QueryUtils.handleSelectQueryExceptions(e)
                }

                checkResultSetColumnCount(resultSet)

                while (resultSet.next()) {
                    returnList.add(processRow(resultSet))
                }

                return returnList
            }
        }
    }

    @Deprecated("Do not use, only public because it's needed for inline functions")
    inline fun <reified T> get(resultSet: ResultSet): T {
        return resultSet.getObject(1, T::class.java)
    }


    /**
     * PRIVATE METHODS
     */

    private fun checkResultSetColumnCount(resultSet: ResultSet) {
        val columnCount = resultSet.metaData.columnCount
        if (columnCount == 0) {
            throw KotgresNoColumnsReturnedException()
        } else if (columnCount > 1) {
            throw KotgresTooManyColumnsReturnedException()
        }
    }


}