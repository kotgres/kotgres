@file:Suppress("TooManyFunctions")

package io.kotgres.orm.dao

import io.kotgres.dsl.ConflictSet
import io.kotgres.dsl.extensions.raw
import io.kotgres.dsl.queries.insert.OnConflictInsertQuery
import io.kotgres.dsl.queries.insert.base.InsertQuery
import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.dao.model.DaoColumnInfo
import io.kotgres.orm.dao.model.ValueWithMapper
import io.kotgres.orm.dao.model.onconflict.dsl.OnConflictResolution
import io.kotgres.orm.dao.model.onconflict.dsl.final.OnConflictIgnore
import io.kotgres.orm.dao.model.onconflict.dsl.final.OnConflictUpdate
import io.kotgres.orm.exceptions.base.KotgresException
import io.kotgres.orm.exceptions.internal.KotgresInternalException
import io.kotgres.orm.exceptions.query.KotgresCannotInsertToGeneratedAlwaysException
import io.kotgres.orm.exceptions.query.KotgresUnexpectedResultsException
import io.kotgres.orm.transactions.KotgresTransaction
import org.postgresql.util.PSQLException

private fun getUpdatedMultipleRowsException(updatedEntities: List<Any>): KotgresException {
    return KotgresInternalException("Updated multiple rows (${updatedEntities.size}, but expected zero or one")
}

private val cannotInsertToGeneratedAlwaysExceptionRegex =
    Regex("""ERROR: cannot insert a non-DEFAULT value into column\s+"([^"]+)"\s+.*"""")

abstract class PrimaryKeyDao<E : Any, I : Any>(conn: AbstractKotgresConnectionPool) :
    AbstractDao<E>(conn) {

    /**
     * ABSTRACT PROPERTIES
     */

    protected abstract val primaryKeyFieldInfo: DaoColumnInfo<E, I>

    /**
     * PROTECTED CACHE PROPERTIES
     */
    private val insertReturningIdColumn by lazy { "RETURNING ${primaryKeyFieldInfo.columnName}" }
    private val updateByPrimaryKeyBaseStatement by lazy {
        io.kotgres.dsl.update(tableName)
            .setList(updatePairs)
            .where("${primaryKeyFieldInfo.columnName} = ?".raw)
            .toSql(true)
    }
    private val updateByPrimaryKeyStatementWithoutReturning by lazy {
        """
            $updateByPrimaryKeyBaseStatement
        """.trimIndent()
    }
    private val updateByPrimaryKeyStatementWithReturning by lazy {
        """
            $updateByPrimaryKeyBaseStatement
            $returningAllColumns
        """.trimIndent()
    }

    /**
     * ABSTRACT METHODS
     */

    protected abstract fun getPrimaryKeyValue(entity: E): I

    /**
     * PUBLIC METHODS: get
     */

    fun getByPrimaryKey(value: I, trx: KotgresTransaction? = null): E? {
        return getByUniqueColumn(primaryKeyFieldInfo.columnName, value, trx)
    }

    fun getByPrimaryKeyList(valueList: List<I>, trx: KotgresTransaction? = null): List<E> {
        return getByUniqueColumnList(primaryKeyFieldInfo.columnName, valueList, trx)
    }

    /**
     * PUBLIC METHODS: insert
     */

    fun insertVoid(entity: E, trx: KotgresTransaction? = null): Int {
        val (query: InsertQuery, bindingWithMapperList: List<ValueWithMapper>) = buildBaseInsertQuery(entity)
        return runDataManipulationQueryVoid(query.toSql(), bindingWithMapperList, trx)
    }

    fun insertVoid(entityList: List<E>, trx: KotgresTransaction? = null): Int {
        val (query: InsertQuery, bindingWithMapperList: List<ValueWithMapper>) = buildBaseInsertQueryList(entityList)
        return runDataManipulationQueryVoid(query.toSql(), bindingWithMapperList, trx)
    }

    fun insertReturningId(entity: E, trx: KotgresTransaction? = null): I {
        val (query: InsertQuery, bindingWithMapperList: List<ValueWithMapper>) = buildBaseInsertQuery(entity)

        // TODO why does the above not work? Something wrong with the DSL?
//        val finalQuery = query
//            .returning(primaryKeyFieldInfo.columnName)
//            .toString()
        val finalQuery = """
            ${query}
            RETURNING ${primaryKeyFieldInfo.columnName}
        """.trimIndent()

        val updatedRowsIds = this.runDataManipulationQueryReturningIdList(finalQuery, bindingWithMapperList, trx)

        return updatedRowsIds.firstOrNull() ?: throw KotgresInternalException("Inserted no rows, but expected exactly one")
    }

    fun insertListReturningId(entityList: List<E>, trx: KotgresTransaction? = null): List<I> {
        val (query: OnConflictInsertQuery, bindingWithMapperList: List<ValueWithMapper>) = buildBaseInsertQueryList(
            entityList
        )

        val finalQuery = query
            .returning(primaryKeyFieldInfo.columnName)
            .toSql(true)

        val updatedRowsIds = this.runDataManipulationQueryReturningIdList(finalQuery, bindingWithMapperList, trx)

        return updatedRowsIds
    }

    fun insert(entity: E, trx: KotgresTransaction? = null): E {
        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQuery(entity)

        val finalQuery = """
            ${query.toSql(true)}
            $returningAllColumns
        """.trimIndent()

        val insertedEntities = this.runDataManipulationQueryReturningEntity(finalQuery, bindings, trx)

        return insertedEntities.firstOrNull()
            ?: throw KotgresInternalException("Inserted no rows, but expected exactly one")
    }

    fun insert(entity: E, onConflict: OnConflictResolution, trx: KotgresTransaction? = null): E? {
        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQuery(entity)

        val onConflictStatement = buildOnConflictStatement(onConflict)

        val finalQuery = """
            ${query.toSql(true)}
            $onConflictStatement
            $returningAllColumns
        """.trimIndent()

        val insertedEntities = this.runDataManipulationQueryReturningEntity(finalQuery, bindings, trx)

        return insertedEntities.firstOrNull()
    }

    fun insert(entityList: List<E>, trx: KotgresTransaction? = null): List<E> {
        if (entityList.isEmpty()) return emptyList()

        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQueryList(entityList)

        val finalQuery = """
            ${query.toSql(true)}
            $returningAllColumns
        """.trimIndent()

        val insertedEntities = this.runDataManipulationQueryReturningEntity(finalQuery, bindings, trx)

        if (insertedEntities.size != entityList.size) {
            throw KotgresInternalException("Inserted ${insertedEntities.size} rows, but expected ${entityList.size}")
        }

        return insertedEntities
    }


    fun insert(entityList: List<E>, onConflict: OnConflictResolution, trx: KotgresTransaction? = null): List<E> {
        if (entityList.isEmpty()) return emptyList()

        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQueryList(entityList)

        val onConflictStatement = buildOnConflictStatement(onConflict)

        val finalQuery = """
            ${query.toSql(true)}
            $onConflictStatement
            $returningAllColumns
        """.trimIndent()

        val insertedEntities = this.runDataManipulationQueryReturningEntity(finalQuery, bindings, trx)

        return insertedEntities
    }

    /**
     * UPSERT DO NOTHING
     */

    /**
     * Upserts with on conflict by a column list, and on conflict does nothing
     * @return null if upsert did nothing, entity otherwise
     */
    fun upsertOnConflictDoNothing(entity: E, columns: List<String>, trx: KotgresTransaction? = null): E? {
        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQuery(entity)

        val finalQuery = query
            .onConflictColumnList(columns)
            .doNothing()
            .returning(allColumns)
            .toSql(true)

        return upsertOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     * Upserts with on conflict by a column list, and on conflict does nothing
     */
    fun upsertListOnConflictDoNothing(
        entityList: List<E>,
        columns: List<String>,
        trx: KotgresTransaction? = null
    ): List<E> {
        if (entityList.isEmpty()) return emptyList()

        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQueryList(entityList)

        val finalQuery = query
            .onConflictColumnList(columns)
            .doNothing()
            .returning(allColumns)
            .toSql(true)

        return upsertListOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     * Upserts with on conflict by a column list, and on conflict does nothing
     * @return null if upsert did nothing, entity otherwise
     */
    fun upsertOnConflictDoNothing(entity: E, constraintName: String, trx: KotgresTransaction? = null): E? {
        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQuery(entity)

        val finalQuery = query
            .onConflictConstraint(constraintName)
            .doNothing()
            .returning(allColumns)
            .toSql(true)

        return upsertOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     *
     * Upserts with on conflict by a column list, and on conflict does nothing
     */
    fun upsertListOnConflictDoNothing(
        entityList: List<E>,
        constraintName: String,
        trx: KotgresTransaction? = null
    ): List<E> {
        if (entityList.isEmpty()) return emptyList()

        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQueryList(entityList)

        val finalQuery = query
            .onConflictConstraint(constraintName)
            .doNothing()
            .returning(allColumns)
            .toSql(true)

        return upsertListOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     * UPSERT MERGE
     */

    /**
     * Upserts with on conflict by a column list, and on conflict does nothing
     * @return null if upsert did nothing, entity otherwise
     */
    fun upsertOnConflictMerge(
        entity: E,
        columns: List<String>,
        conflictSetList: List<ConflictSet>,
        trx: KotgresTransaction? = null
    ): E? {
        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQuery(entity)

        val finalQuery = query
            .onConflictColumnList(columns)
            .doUpdate(conflictSetList)
            .returning(allColumns)
            .toSql(true)

        return upsertOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     * Upserts with on conflict by a column list, and on conflict does nothing
     */
    fun upsertListOnConflictMerge(
        entityList: List<E>,
        columns: List<String>,
        conflictSetList: List<ConflictSet>,
        trx: KotgresTransaction? = null
    ): List<E> {
        if (entityList.isEmpty()) return emptyList()

        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQueryList(entityList)

        val finalQuery = query
            .onConflictColumnList(columns)
            .doUpdate(conflictSetList)
            .returning(allColumns)
            .toSql(true)

        return upsertListOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     * Upserts with on conflict by a column list, and on conflict does nothing
     * @return null if upsert did nothing, entity otherwise
     */
    fun upsertOnConflictMerge(
        entity: E, constraintName: String,
        conflictSetList: List<ConflictSet>,
        trx: KotgresTransaction? = null
    ): E? {
        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQuery(entity)

        val finalQuery = query
            .onConflictConstraint(constraintName)
            .doUpdate(conflictSetList)
            .returning(allColumns)
            .toSql(true)

        return upsertOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     *
     * Upserts with on conflict by a column list, and on conflict does nothing
     */
    fun upsertListOnConflictMerge(
        entityList: List<E>,
        constraintName: String,
        conflictSetList: List<ConflictSet>,
        trx: KotgresTransaction? = null
    ): List<E> {
        if (entityList.isEmpty()) return emptyList()

        val (query: OnConflictInsertQuery, bindings: List<ValueWithMapper>) = buildBaseInsertQueryList(entityList)

        val finalQuery = query
            .onConflictConstraint(constraintName)
            .doUpdate(conflictSetList)
            .returning(allColumns)
            .toSql(true)

        return upsertListOnConflictInternal(finalQuery, bindings, trx)
    }

    /**
     * PUBLIC METHODS: update
     */

    /**
     * Returns null if the entity did not exist in the database
     */
    fun update(entity: E, trx: KotgresTransaction? = null): E? {
        val primaryKeyValue = getPrimaryKeyValue(entity)

        val bindingWithMapperList = buildBindingsForUpdate(entity).toMutableList()

        // TODO is this something we want to do??? I store only if PKey is updateable, which in general should not
        bindingWithMapperList.add(ValueWithMapper(primaryKeyValue, primaryKeyFieldInfo.mapper))

        val updatedEntities = this.runDataManipulationQueryReturningEntity(
            updateByPrimaryKeyStatementWithReturning,
            bindingWithMapperList,
            trx,
        )

        return when (updatedEntities.size) {
            0 -> null
            1 -> updatedEntities.first()
            else -> throw getUpdatedMultipleRowsException(updatedEntities)
        }
    }

    fun updateReturningIds(entity: E, trx: KotgresTransaction? = null): List<I> {
        val primaryKeyValue = getPrimaryKeyValue(entity)

        val bindingWithMapperList = buildBindingsForUpdate(entity).toMutableList()

        // TODO is this something we want to do??? I store only if PKey is updateable, which in general should not
        bindingWithMapperList.add(ValueWithMapper(primaryKeyValue, primaryKeyFieldInfo.mapper))

        return this.runDataManipulationQueryReturningIdList(
            updateByPrimaryKeyStatementWithReturning,
            bindingWithMapperList,
            trx,
        )
    }

    fun updateVoid(entity: E, trx: KotgresTransaction? = null): Int {
        val primaryKeyValue = getPrimaryKeyValue(entity)

        val bindingWithMapperList = buildBindingsForUpdate(entity).toMutableList()

        // TODO is this something we want to do??? I store only if PKey is updateable, which in general should not
        bindingWithMapperList.add(ValueWithMapper(primaryKeyValue, primaryKeyFieldInfo.mapper))

        return this.runDataManipulationQueryVoid(
            updateByPrimaryKeyStatementWithoutReturning,
            bindingWithMapperList,
            trx
        )
    }

    /**
     * PUBLIC METHODS: delete
     */

    fun delete(entity: E, trx: KotgresTransaction? = null): Boolean {
        return deleteById(getPrimaryKeyValue(entity), trx)
    }

    // TODO test
    fun deleteList(entityList: List<E>, trx: KotgresTransaction? = null): Boolean {
        val idList = entityList.map { getPrimaryKeyValue(it) }
        return deleteByColumnValueList(primaryKeyFieldInfo.columnName, idList, trx) == 1
    }

    fun deleteById(id: I, trx: KotgresTransaction? = null): Boolean {
        return deleteByColumnValue(primaryKeyFieldInfo.columnName, id, trx) == 1
    }

    // TODO test
    fun deleteByIdList(idList: List<I>, trx: KotgresTransaction? = null): Boolean {
        return deleteByColumnValueList(primaryKeyFieldInfo.columnName, idList, trx) == 1
    }

    /**
     * PRIVATE METHODS
     */

    private fun buildBaseInsertQuery(entity: E): Pair<OnConflictInsertQuery, List<ValueWithMapper>> {
        val bindingWithMapperList = if (isInsertEmpty) {
            listOf()
        } else {
            buildBindingsForInsert(entity)
        }

        return Pair(insertStatementQuery.clone(), bindingWithMapperList)
    }

    private fun buildBaseInsertQueryList(entityList: List<E>): Pair<OnConflictInsertQuery, List<ValueWithMapper>> {
        val bindingWithMapperList = if (isInsertEmpty) {
            listOf()
        } else {
            entityList.map { entity ->
                buildBindingsForInsert(entity)
            }.flatten()
        }

        val listInsertStatement = getInsertStatementForList(entityList)

        return Pair(listInsertStatement, bindingWithMapperList)
    }

    private fun runDataManipulationQueryVoid(
        query: String,
        bindings: List<ValueWithMapper>,
        trx: KotgresTransaction?
    ): Int {
        maybeLogQuery(query)
        return useDaoConnection(trx) { conn ->
            val preparedStatement = conn.prepareStatement(query)
            addBindingsToPreparedStatement(bindings, preparedStatement, conn)

            return@useDaoConnection preparedStatement.executeUpdate()
        }
    }

    private fun runDataManipulationQueryVoid(query: String, trx: KotgresTransaction?): Int {
        maybeLogQuery(query)
        return runDataManipulationQueryVoid(query, listOf(), trx)
    }

    private fun runDataManipulationQueryReturningIdList(
        query: String,
        bindings: List<ValueWithMapper>,
        trx: KotgresTransaction?
    ): List<I> {
        maybeLogQuery(query)
        return useDaoConnection(trx) { conn ->
            return@useDaoConnection conn.prepareStatement(query).use { preparedStatement ->
                addBindingsToPreparedStatement(bindings, preparedStatement, conn)

                val success = preparedStatement.execute()
                if (!success) throw KotgresUnexpectedResultsException(null)

                val rs = preparedStatement.resultSet
                val listOfIds = mutableListOf<I>()
                while (rs.next()) {
                    listOfIds.add(primaryKeyFieldInfo.mapper.getFromResultSet(rs, 1))
                }
                listOfIds
            }
        }
    }

    private fun runDataManipulationQueryReturningEntity(
        query: String,
        bindings: List<ValueWithMapper>,
        trx: KotgresTransaction?
    ): List<E> {
        maybeLogQuery(query)
        return useDaoConnection(trx) { conn ->
            return@useDaoConnection conn.prepareStatement(query).use { preparedStatement ->
                addBindingsToPreparedStatement(bindings, preparedStatement, conn)

                val success = try {
                    preparedStatement.execute()
                } catch (e: PSQLException) {
                    if (e.message == null) throw e

                    // first filter before throwing exception below
                    if (!e.message!!.contains("GENERATED ALWAYS")) {
                        throw e
                    }

                    val match = cannotInsertToGeneratedAlwaysExceptionRegex.find(e.message!!) ?: throw e

                    val columnName = match.groupValues[1]
                    throw KotgresCannotInsertToGeneratedAlwaysException(e, columnName)
                }
                if (!success) throw KotgresUnexpectedResultsException(null)

                val resultSet = preparedStatement.resultSet

                mapResultSetToEntities(resultSet)
            }
        }
    }

    private fun runDataManipulationQueryReturningEntity(query: String, trx: KotgresTransaction?): List<E> {
        maybeLogQuery(query)
        return runDataManipulationQueryReturningEntity(query, listOf(), trx)
    }

    // TODO this could be deleted by using DSL
    private fun buildOnConflictStatement(onConflict: OnConflictResolution): String {
        val onConflictStatement = run {
            val postFix: String = when (onConflict) {
                is OnConflictIgnore -> {
                    "DO NOTHING"
                }

                is OnConflictUpdate -> {
                    val setExpresions = onConflict.columnToExpressionMap.joinToString(", ") {
                        "${it.columnName} = ${it.setExpression}"
                    }
                    "DO UPDATE \nSET $setExpresions"
                }

                else -> {
                    throw KotgresInternalException("Unknown type of onConflict - $onConflict")
                }
            }
            "ON CONFLICT ${onConflict.target} $postFix"
        }
        return onConflictStatement
    }

    private fun upsertOnConflictInternal(
        query: String,
        bindings: List<ValueWithMapper>,
        trx: KotgresTransaction?
    ): E? {
        val updatedEntities = this.runDataManipulationQueryReturningEntity(query, bindings, trx)

        return when (updatedEntities.size) {
            0 -> null
            1 -> updatedEntities.first()
            else -> throw getUpdatedMultipleRowsException(updatedEntities)
        }
    }

    private fun upsertListOnConflictInternal(
        query: String,
        bindings: List<ValueWithMapper>,
        trx: KotgresTransaction?
    ): List<E> {
        return this.runDataManipulationQueryReturningEntity(query, bindings, trx)
    }
}
