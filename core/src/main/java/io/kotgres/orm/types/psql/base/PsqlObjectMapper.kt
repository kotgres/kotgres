package io.kotgres.orm.types.psql.base

import io.kotgres.orm.exceptions.entity.KotgresEntityDefinitionException
import io.kotgres.orm.exceptions.misc.KotgresPostgresInternalException
import io.kotgres.orm.types.TypeResolver
import io.kotgres.orm.types.base.AbstractMapper
import io.kotgres.orm.internal.utils.joinWithDifferentLastSeparator
import org.postgresql.util.PSQLException
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.ResultSet
import kotlin.reflect.KClass

// TODO is there a way to not have kClass in constructor? Only inlining? Inlining would be bad in this case

// TODO docs: add links to correct docs
private const val CUSTOM_MAPPER_SUGGESTION = "following these docs <link>"

/**
 * Mapper for objects that JDBC already knows how to convert
 */
abstract class PsqlObjectMapper<T : Any>(
    private val tClass: KClass<T>,
    nullable: Boolean = false,
) : AbstractMapper<T>(tClass, nullable) {
    override fun fromSql(
        resultSet: ResultSet,
        position: Int,
    ): T? {
        checkAllowedTypes(resultSet, position)

        return try {
            resultSet.getObject(position, tClass.java)
        } catch (e: PSQLException) {
            throw parseGetObjectException(e)
        }
    }

    private fun parseGetObjectException(e: PSQLException): Exception {
        val regex = Regex("^Cannot convert the column of type (.*) to requested type (.*)\\.\$")
        val match = regex.matchEntire(e.message.toString())

        if (match == null || match.groups.size != 3) {
            return KotgresPostgresInternalException(e)
        }

        val fromType = match.groups[1]!!.value.trim()
        val toType = match.groups[2]!!.value.trim()

//        val mapper = null
//        var postFix =
//            "No built-in mapper found for this type. Please "
//        if (mapper !== null) {
//            // result type does not match
//            if (true) {
//                postFix =
//                    "To use the built-in mapper, change your JVM type from X to Y. You can custom mapper from T to Y ${CUSTOM_MAPPER_SUGGESTION}"
//            }
//        }
        return KotgresPostgresInternalException("Could not convert type $fromType to type $toType.")
    }

    private fun checkAllowedTypes(
        resultSet: ResultSet,
        position: Int,
    ) {
        // just run the check if there are types (null is an exception for enums)
        if (postgresTypes == null) {
            return
        }

        val columnPgType = resultSet.metaData.getColumnTypeName(position).lowercase()

        val allowedType = postgresTypes!!.contains(columnPgType)

        if (!allowedType) {
            val correctTypeSuggestion = getCorrectTypeSuggestion(columnPgType)

            throw KotgresEntityDefinitionException(
                """
                    Cannot map ${columnPgType.uppercase()} to $kotlinClassName. To fix this,$correctTypeSuggestion create a custom mapper for $kotlinClassName $CUSTOM_MAPPER_SUGGESTION. 
                """.trim(),
            )
        }
    }

    private fun getCorrectTypeSuggestion(columnPgType: String): String {
        val correctTypeList = TypeResolver.getSingleton().getJavaType(columnPgType)

        if (correctTypeList == null) {
            return ""
        }

        return " use ${correctTypeList.joinWithDifferentLastSeparator(", ", " or ")} instead in your model or"
    }

    override fun toSql(
        value: T,
        preparedStatement: PreparedStatement,
        position: Int,
        conn: Connection
    ) {
        preparedStatement.setObject(position, value)
    }
}
