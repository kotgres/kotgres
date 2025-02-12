@file:Suppress("TooManyFunctions")

package io.kotgres.orm.dao

import com.squareup.kotlinpoet.asClassName
import io.kotgres.dsl.deleteFrom
import io.kotgres.dsl.extensions.raw
import io.kotgres.dsl.insertInto
import io.kotgres.dsl.operators.eq
import io.kotgres.dsl.operators.eqAny
import io.kotgres.dsl.queries.base.IDataManipulationQuery
import io.kotgres.dsl.queries.delete.UsingDeleteQuery
import io.kotgres.dsl.queries.delete.base.DeleteQuery
import io.kotgres.dsl.queries.insert.ColumnsInsertQuery
import io.kotgres.dsl.queries.insert.OnConflictInsertQuery
import io.kotgres.dsl.queries.insert.base.InsertQuery
import io.kotgres.dsl.queries.select.JoinQuerySelect
import io.kotgres.dsl.queries.select.base.SelectQuery
import io.kotgres.dsl.queries.update.SetQueryUpdate
import io.kotgres.dsl.queries.update.base.UpdateQuery
import io.kotgres.dsl.select
import io.kotgres.dsl.update
import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.dao.model.DaoColumnInfo
import io.kotgres.orm.dao.model.ValueWithMapper
import io.kotgres.orm.exceptions.dao.KotgresColumnNotFoundInEntityException
import io.kotgres.orm.exceptions.dao.KotgresColumnNotFoundInQueryResultException
import io.kotgres.orm.exceptions.dao.KotgresDaoUnexpectedReturningException
import io.kotgres.orm.exceptions.dao.KotgresUnexpectedGetByUniqueResultsException
import io.kotgres.orm.exceptions.internal.KotgresInternalException
import io.kotgres.orm.exceptions.query.KotgresBindingsMatchException
import io.kotgres.orm.internal.ApLogger
import io.kotgres.orm.internal.utils.Debug
import io.kotgres.orm.internal.utils.QueryUtils
import io.kotgres.orm.transactions.KotgresTransaction
import io.kotgres.orm.types.TypeResolver
import io.kotgres.orm.types.base.AbstractMapper
import io.kotgres.orm.types.custom.EnumMapper
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Statement
import java.sql.Types

// public should ideally be private but needs to be public due to inline functions
abstract class AbstractDao<E>(val pool: AbstractKotgresConnectionPool) {

    /**
     * DEPENDENCIES
     */
    private val typeResolver = TypeResolver.getSingleton()

    /**
     * PRIVATE FIELDS FOR CACHING
     */
    private val columnNotFoundExceptionRegex = Regex("""The column name (\w+) was not found in this ResultSet.""")

    /**
     * ABSTRACT FIELDS
     */

    protected abstract val className: String
    protected abstract val allFields: Map<String, DaoColumnInfo<E, *>>
    abstract val tableName: String

    /**
     * PROTECTED FIELDS FOR INSERT CACHING
     */
    protected val insertFields: Map<String, DaoColumnInfo<E, *>> by lazy {
        allFields.filter { (_, v) -> !v.isGenerated }
    }
    protected val insertColumns by lazy { insertFields.values.map { it.columnName } }
    protected val insertColumnsString by lazy { insertColumns.joinToString(", ") }
    protected val insertBindingString by lazy { List(insertFields.size) { _ -> "?" }.joinToString(", ") }
    protected val allColumns by lazy {
        allFields.values.joinToString(", ") { it.columnName }
    }
    protected val returningAllColumns by lazy {
        "RETURNING $allColumns"
    }
    protected val isInsertEmpty by lazy { insertFields.isEmpty() }
    protected val insertStatementQuery: OnConflictInsertQuery by lazy {
        if (isInsertEmpty) {
            insertInto(tableName).defaultValues()
        } else {
            insertInto(tableName)
                .columns(insertColumnsString.raw)
                .valueRaw(insertBindingString.raw)
        }
    }
    protected val insertStatement by lazy {
        insertStatementQuery.toSql(true)
    }


    /**
     * PROTECTED FIELDS FOR UPDATE CACHING
     */
    protected val updateFields: Map<String, DaoColumnInfo<E, *>> by lazy {
        allFields.filter { (_, fieldInfo) -> fieldInfo.allowUpdates }
    }
    protected val updatePairs: List<Pair<String, String>> by lazy {
        updateFields.map { (_, v) -> v.columnName to "?" }
    }

    /**
     * PROTECTED FIELDS FOR DELETE CACHING
     */

    /**
     * INTERNAL FIELD FOR INLINE FUNCTION
     */
//    @PublishedApi
//    internal val `access$declaredFields`: Map<String, Field>
//        get() = declaredFields

    /**
     * ABSTRACT METHODS
     */
    abstract fun mapQueryResult(result: ResultSet): E
    abstract fun getNewInstance(): AbstractDao<E>

    /**
     * PUBLIC METHODS: get
     */

    fun getAll(trx: KotgresTransaction? = null): List<E> {
        return useDaoStatement(trx) { st ->
            val queryString = select("*")
                .from(tableName)
                .toSql(true)

            val resultSet = st.executeQuery(queryString)

            return@useDaoStatement mapResultSetToEntities(resultSet)
        }
    }

    fun getByUniqueColumn(columnName: String, value: Any?, trx: KotgresTransaction? = null): E? {
        val query = select("*")
            .from(tableName)
            .where(columnName eq "?".raw)

        val returnValues = runSelect(query, listOf(value), trx)

        return if (returnValues.isEmpty()) {
            null
        } else if (returnValues.size == 1) {
            returnValues[0]
        } else {
            throw KotgresUnexpectedGetByUniqueResultsException()
        }
    }

    fun getByUniqueColumnList(columnName: String, valueList: List<Any?>, trx: KotgresTransaction? = null): List<E> {
        if (valueList.isEmpty()) return emptyList()


        val query = select("*")
            .from(tableName)
            .where(columnName eqAny "?".raw)

        return runSelect(query, listOf(valueList), trx)
    }

    /**
     * PUBLIC METHODS: delete
     */
    fun deleteByColumnValue(columnName: String, value: Any?, trx: KotgresTransaction? = null): Int {
        val deletedRowsCount = this.runDataManipulation(
            """
                DELETE FROM $tableName
                WHERE $columnName = ?
            """.trimIndent(),
            ValueWithMapper(value, getMapperByColumn(columnName)),
            trx,
        )
        return deletedRowsCount
    }

    fun deleteByColumnValueList(columnName: String, value: List<Any?>, trx: KotgresTransaction? = null): Int {
        val deletedRowsCount = this.runDataManipulation(
            """
                DELETE FROM $tableName
                WHERE $columnName IN ?
            """.trimIndent(),
            ValueWithMapper(value, getMapperByColumn(columnName)),
            trx,
        )
        return deletedRowsCount
    }

    /**
     * PUBLIC METHODS: any query void
     */
    fun runQueryVoid(query: String, trx: KotgresTransaction? = null) {
        return useDaoStatement(trx) { st ->
            st.execute(query)
        }
    }

    /**
     * Returns a single element for a query that returns only one column
     * Only supports types that are natively supported by JDBC
     * Non-exhaustive list: String, Boolean, Int, Double, Long, Date, LocalDateTime, ...
     */
    inline fun <reified T> runSelectQueryReturningOne(query: String, trx: KotgresTransaction? = null): T? {
        return pool.runSelectQueryReturningOne<T>(query)
    }

    /**
     * Returns many element for a query that returns only one column
     * Only supports types that are natively supported by JDBC
     * Non-exhaustive list: String, Boolean, Int, Double, Long, Date, LocalDateTime, ...
     */
    inline fun <reified T> runSelectQueryReturningList(query: String, trx: KotgresTransaction? = null): List<T> {
        return pool.runSelectQueryReturningList<T>(query)
    }

    /**
     * PUBLIC METHODS: select query
     */

    fun selectQuery(): JoinQuerySelect {
        return select("*").from(tableName)
    }

    // TODO we need to match the columns the user is using for the bindings
    // TODO to find the mappers and be able to use them effectively
    fun runSelect(query: String, bindings: List<Any?>, trx: KotgresTransaction? = null): List<E> {
        checkNumberOfBindings(query, bindings)

        maybeLogQuery(query)

        return useDaoPreparedStatement(trx, query) { st, conn ->
            addBindingsToPreparedStatementWithoutMapper(bindings, st, conn)

            val returnValues = try {
                val resultSet = st.executeQuery()
                mapResultSetToEntities(resultSet)
            } catch (e: PSQLException) {
                throw QueryUtils.handleSelectQueryExceptions(e)
            }

            return@useDaoPreparedStatement returnValues
        }
    }

    fun runSelect(query: String, trx: KotgresTransaction? = null): List<E> {
        return runSelect(query, listOf(), trx)
    }

    fun runSelect(selectQuery: SelectQuery, bindings: List<Any?>, trx: KotgresTransaction? = null): List<E> {
        val query = selectQuery.toSql(true)
        return runSelect(query, bindings, trx)
    }

    fun runSelect(selectQuery: SelectQuery, trx: KotgresTransaction? = null): List<E> {
        return runSelect(selectQuery, listOf(), trx)
    }

    /**
     * PUBLIC METHODS: insert query
     */
    fun insertQuery(): ColumnsInsertQuery {
        return insertInto(tableName)
    }

    /**
     * Returns the number of updated rows
     */
    fun runInsert(query: String, trx: KotgresTransaction? = null): Int {
        return runDataManipulationQueryWithMappers(query, listOf(), trx)
    }

    fun runInsert(insertQuery: InsertQuery, bindings: List<Any?>, trx: KotgresTransaction? = null): Int {
        checkDataManipulationQuery(insertQuery)

        val query = insertQuery.toSql(true)
        return runDataManipulationQueryWithoutMappers(query, bindings, trx)
    }

    fun runInsert(insertQuery: InsertQuery, trx: KotgresTransaction? = null): Int {
        return runInsert(insertQuery, listOf(), trx)
    }

    /**
     * PUBLIC METHODS: update query
     */

    fun updateQuery(): SetQueryUpdate {
        return update(tableName)
    }

    fun runUpdate(query: String, bindings: List<Any?>, trx: KotgresTransaction? = null): Int {
        return runDataManipulationQueryWithoutMappers(query, bindings, trx)
    }

    fun runUpdate(query: String, trx: KotgresTransaction? = null): Int {
        return runUpdate(query, listOf(), trx)
    }

    fun runUpdate(updateQuery: UpdateQuery, bindings: List<Any?>, trx: KotgresTransaction? = null): Int {
        checkDataManipulationQuery(updateQuery)

        val query = updateQuery.toSql(true)
        return runUpdate(query, bindings, trx)
    }

    fun runUpdate(updateQuery: UpdateQuery, trx: KotgresTransaction? = null): Int {
        return runUpdate(updateQuery, listOf(), trx)
    }

    /**
     * PUBLIC METHODS: delete query
     */
    fun deleteQuery(): UsingDeleteQuery {
        return deleteFrom(tableName)
    }

    fun runDelete(query: String, trx: KotgresTransaction? = null): Int {
        return runDataManipulationQueryWithMappers(query, listOf(), trx)
    }

    fun runDelete(deleteQuery: DeleteQuery, bindings: List<Any?>, trx: KotgresTransaction? = null): Int {
        checkDataManipulationQuery(deleteQuery)

        val query = deleteQuery.toSql(true)
        return runDataManipulationQueryWithoutMappers(query, bindings, trx)
    }

    fun runDelete(deleteQuery: DeleteQuery, trx: KotgresTransaction? = null): Int {
        return runDelete(deleteQuery, listOf(), trx)
    }

    /**
     * PROTECTED METHODS
     */

    protected fun <T> useDaoConnection(trx: KotgresTransaction?, block: (conn: Connection) -> T): T {
        return if (trx != null) {
            // we don't do a use here because we don't want to close it
            block(trx.connection)
        } else {
            pool.getConnection().use {
                return@use block(it)
            }
        }
    }

    /**
     * Data Manipulation Query means: insert, update or delete queries
     */
    protected fun runDataManipulationQueryWithoutMappers(
        query: String,
        bindings: List<Any?>,
        trx: KotgresTransaction?
    ): Int {
        checkNumberOfBindings(query, bindings)

        return useDaoPreparedStatement(trx, query) { st, conn ->
            addBindingsToPreparedStatementWithoutMapper(bindings, st, conn)

            val updatedRowsCount = try {
                st.executeUpdate()
            } catch (e: PSQLException) {
                throw QueryUtils.handleUpdateQueryExceptions(e)
            }

            return@useDaoPreparedStatement updatedRowsCount
        }
    }

    /**
     * Data Manipulation Queries means: insert, update or delete queries
     */
    protected fun runDataManipulationQueryWithMappers(
        query: String,
        bindings: List<ValueWithMapper>,
        trx: KotgresTransaction?
    ): Int {
        checkNumberOfBindings(query, bindings)

        return useDaoPreparedStatement(trx, query) { st, conn ->
            addBindingsToPreparedStatement(bindings, st, conn)

            val updatedRowsCount = try {
                st.executeUpdate()
            } catch (e: PSQLException) {
                throw QueryUtils.handleUpdateQueryExceptions(e)
            }

            return@useDaoPreparedStatement updatedRowsCount
        }
    }

    protected fun runDataManipulation(query: String, binding: ValueWithMapper, trx: KotgresTransaction?): Int {
        return runDataManipulationQueryWithMappers(query, listOf(binding), trx)
    }


    protected fun mapResultSetToEntities(resultSet: ResultSet): MutableList<E> {
        val returnValues = mutableListOf<E>()
        while (resultSet.next()) {
            val entity: E = mapQueryResult(resultSet)
            returnValues.add(entity)
        }
        return returnValues
    }

    // TODO could add <T> as parametrized method and then do as T
    // TODO but it's hard to get working with custom types then, since those would need to be imported by the DAO then
    protected fun setFieldNullable(columnName: String, resultSet: ResultSet, entity: E) {
        val age =
            getMapperByColumn(columnName).getFromResultSetNullable(resultSet, resultSet.findColumn(columnName)) as Any?
        allFields[columnName]!!.declaredField.set(entity, age)
    }

    // TODO could add <T> as parametrized method and then do as T
    // TODO but it's hard to get working with custom types then, since those would need to be imported by the DAO then
    protected fun setField(columnName: String, resultSet: ResultSet, entity: E) {
        val mapper = getMapperByColumn(columnName)
        val age = try {
            mapper.getFromResultSet(resultSet, resultSet.findColumn(columnName)) as Any?
        } catch (e: PSQLException) {
            if (e.message == null) throw e
            if (!e.message!!.startsWith("The column name")) throw e
            val matchResult = columnNotFoundExceptionRegex.find(e.message!!)
            val errorColumnName = matchResult?.groups?.get(1)?.value!!
            throw KotgresColumnNotFoundInQueryResultException(e, errorColumnName)
        }
        allFields[columnName]!!.declaredField.set(entity, age)
    }

    // TODO risky bridge here, may impact performance
    protected inline fun <reified T : Enum<*>> setFieldEnum(
        columnName: String,
        resultSet: ResultSet,
        entity: E,
    ) {
        val type = EnumMapper(false).getFromResultSetInternal(resultSet, resultSet.findColumn(columnName)) as T
        val field = allFields[columnName]!!.declaredField
        field.set(entity, type)
    }

    protected fun addBindingsToPreparedStatement(
        bindings: Array<out ValueWithMapper>,
        preparedStatement: PreparedStatement,
        conn: Connection
    ) {
        return addBindingsToPreparedStatement(bindings.toList(), preparedStatement, conn)
    }

    protected fun addBindingsToPreparedStatement(
        bindings: List<ValueWithMapper>,
        preparedStatement: PreparedStatement,
        conn: Connection
    ) {
        bindings.forEachIndexed { loopIndex, binding ->
            val index = loopIndex + 1
            binding.mapper.addToStatement(binding.value, preparedStatement, index, conn)
        }
    }

    protected fun getMapperByColumn(columnName: String): AbstractMapper<*> =
        allFields[columnName]?.mapper ?: throw KotgresColumnNotFoundInEntityException(columnName, allFields.keys.toList())

    protected fun buildBindingsForInsert(entity: E): List<ValueWithMapper> {
        return insertFields.entries
            .map { (columnName, fieldInfo) ->
                ValueWithMapper(
                    fieldInfo.getValue(entity),
                    getMapperByColumn(columnName),
                )
            }
            .toList()
    }

    @Deprecated("use buildBindingWithMapperListForInsert")
    protected fun buildBindingsForInsertArray(entity: E): Array<ValueWithMapper> {
        return insertFields.entries
            .map { (columnName, fieldInfo) ->
                ValueWithMapper(
                    fieldInfo.getValue(entity),
                    getMapperByColumn(columnName),
                )
            }
            .toTypedArray()
    }

    protected fun buildBindingsForUpdate(entity: E): Array<ValueWithMapper> {
        return updateFields.entries
            .map { (columnName, fieldInfo) ->
                ValueWithMapper(
                    fieldInfo.getValue(entity),
                    getMapperByColumn(columnName),
                )
            }
            .toTypedArray()
    }

    protected fun getInsertStatementForList(list: List<E>): OnConflictInsertQuery {
        return if (isInsertEmpty) {
            val defaultValuesStatement = List(list.size) { _ -> "(default)".raw }
            insertInto(tableName)
                .columns(listOf())
                .valuesRawList(defaultValuesStatement)
        } else {
            val bindingList = List(list.size) { _ -> insertBindingString.raw }
            insertInto(tableName)
                .columns(insertColumns)
                .valuesRawList(bindingList)
        }
    }

    /**
     * INTERNAL METHODS
     */
    internal fun maybeLogQuery(query: String) {
        if (!Debug.ENABLED) {
            return
        }
        ApLogger.debug("Running query below")
        ApLogger.debug(query)
    }

    /**
     * PRIVATE METHODS
     */

    private fun addBindingsToPreparedStatementWithoutMapper(
        bindings: List<Any?>,
        preparedStatement: PreparedStatement,
        conn: Connection
    ) {
        //  TODO use with indexed
        var bindingPosition = 1
        for (binding in bindings) {
            if (binding == null) {
                preparedStatement.setNull(bindingPosition, Types.NULL)
            } else {

                val listParameterType: String? = if (binding is List<*>) {
                    val first = binding.first()!!
                    first::class.asClassName().canonicalName
                } else {
                    null
                }

                val className = binding::class.asClassName().canonicalName
                val mapperKClass = typeResolver.getMapperKClass(className, false, false, false, null, listParameterType)
                val mapperInstance = TypeResolver.callMapperConstructor(mapperKClass)
                mapperInstance.addToStatement(binding, preparedStatement, bindingPosition, conn)
            }
            bindingPosition++
        }
    }

    private fun checkNumberOfBindings(query: String, bindings: List<Any?>) {
        val numberOfBindingsInQuery = query.count { it == '?' }
        val numberOfBindingsPassed = bindings.size

        if (numberOfBindingsInQuery != numberOfBindingsPassed) {
            throw KotgresBindingsMatchException(numberOfBindingsInQuery, numberOfBindingsPassed)
        }
    }

    private fun addArrayToPreparedStatement(
        binding: List<*>,
        preparedStatement: PreparedStatement,
        index: Int,
    ) {
        if (binding.isNotEmpty()) {
            val firstElement = binding.first()
            // TODO check all elements are same type
            val type = getArrayPostgresType(firstElement)
            val arr = preparedStatement.connection.createArrayOf(type, binding.toTypedArray())
            preparedStatement.setArray(index, arr)
        } else {
            throw KotgresInternalException("This type of list is not yet supported in bindings")
        }
    }

    // TODO fix this code to make it cleaner
    private fun getArrayPostgresType(firstElement: Any?): String {
        val type = when (firstElement) {
            is Int -> {
                "INT"
            }

            is String -> {
                "TEXT"
            }

            else -> {
                throw KotgresInternalException("Unknown list binding type")
            }
        }
        return type
    }

    private fun <T> useDaoStatement(trx: KotgresTransaction?, block: (statement: Statement) -> T): T {
        trx?.verifyIsOpen()

        return useDaoConnection(trx, { conn ->
            conn.createStatement().use(block)
        })
    }

    private fun <T> useDaoPreparedStatement(
        trx: KotgresTransaction?,
        query: String,
        block: (statement: PreparedStatement, conn: Connection) -> T
    ): T {
        return useDaoConnection(trx) { conn ->
            conn.prepareStatement(query).use { st ->
                block(st, conn)
            }
        }
    }

    private fun checkDataManipulationQuery(query: IDataManipulationQuery) {
        if (!query.hasReturning()) return

        throw KotgresDaoUnexpectedReturningException()
    }
}