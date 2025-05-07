package io.kotgres.orm.connection

import io.kotgres.orm.exceptions.query.KotgresNoColumnsReturnedException
import io.kotgres.orm.exceptions.query.KotgresTooManyColumnsReturnedException
import io.kotgres.orm.exceptions.query.KotgresTooManyRowsReturnedException
import io.kotgres.orm.internal.utils.QueryUtils
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.ResultSet

object KotgresConnectionUtils {

    // Only public because it's needed for inline functions
    @Deprecated("Use KotgresConnectionPool.runSelectQueryReturningOne")
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

    // Only public because it's needed for inline functions
    @Deprecated("Use KotgresConnectionPool.runSelectQueryReturningList")
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

    // Only public because it's needed for inline functions
    @Deprecated("Do not use, internal usage only")
    inline fun <reified T> get(resultSet: ResultSet): T {
        return resultSet.getObject(1, T::class.java)
    }

    fun runSelectQueryReturningMap(
        query: String,
        getConnection: () -> Connection
    ): Map<String, String>? {
        getConnection().use { conn ->
            val st = conn.createStatement()
            st.use {
                val resultSet = try {
                    it.executeQuery(query)
                } catch (e: PSQLException) {
                    throw QueryUtils.handleSelectQueryExceptions(e)
                }

                if (!resultSet.next()) {
                    return null
                }

                val metadata = resultSet.metaData
                val columnCount = metadata.columnCount

                val result = (1..columnCount).associate { columnIndex ->
                    val columnName = metadata.getColumnName(columnIndex)
                    columnName to resultSet.getObject(columnIndex).toString()
                }

                val hasMoreResults = resultSet.next()
                if (hasMoreResults) {
                    throw KotgresTooManyRowsReturnedException()
                }

                return result
            }
        }
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