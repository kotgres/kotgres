@file:Suppress("TooManyFunctions")

package io.kotgres.orm.internal.builders

//import com.fasterxml.jackson.databind.JsonNode
//import org.json.JSONObject
import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.toClassName
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.dao.AbstractDao
import io.kotgres.orm.dao.NoPrimaryKeyDao
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.dao.model.DaoColumnInfo
import io.kotgres.orm.exceptions.dao.KotgresCantUpdateNullPrimaryKeyEntityException
import io.kotgres.orm.exceptions.entity.KotgresNoEmptyConstructorException
import io.kotgres.orm.exceptions.internal.KotgresInternalException
import io.kotgres.orm.internal.processors.model.BuilderConstants.INDENTATION
import io.kotgres.orm.internal.processors.model.EntityInfo
import io.kotgres.orm.internal.processors.model.MapperInfo
import io.kotgres.orm.internal.processors.model.PropertyInfo
import io.kotgres.orm.internal.utils.*
import io.kotgres.orm.types.TypeResolver
import io.kotgres.orm.types.base.CustomMapper
import java.lang.reflect.Constructor
import java.sql.ResultSet
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.util.*


// TODO find a way to not always add these to Dao since they bloat it
// TODO disable adding JSON types for now, since they make DAOs crash for project not having those dependencies
private val EXTRA_DATA_TYPES =
    listOf(
        LocalDateTime::class,
        UUID::class,
        Date::class,
        OffsetDateTime::class,
//        JSONObject::class,
//        com.google.gson.JsonElement::class,
//        JsonNode::class,
//        kotlinx.serialization.json.JsonObject::class,
    )

private val EXTRA_EXCEPTION_TYPES =
    listOf(
        KotgresCantUpdateNullPrimaryKeyEntityException::class,
    )

internal class DaoBuilder(
    private val logger: KSPLogger,
    private val entityClassDeclaration: KSClassDeclaration,
    entityInfo: EntityInfo,
) {

    private val fieldsInfo: List<PropertyInfo> = entityInfo.fieldsInfo
    private val primaryKeyInfo: PropertyInfo? = entityInfo.primaryKeyInfo

    fun buildDao(containingFile: KSFile): FileSpec.Builder {
        val packageName = entityClassDeclaration.containingFile!!.packageName.asString()
        val entityName = entityClassDeclaration.simpleName.asString() // this actually could be reused in many places
        val daoClassName = BuilderUtils.entityNameToDaoName(entityName)

        if (Debug.ENABLED) {
            logger.warn("Building DAO for $entityName")
        }

        // Save it for use in DaoManagerBuilder
        val isPrimaryKey = primaryKeyInfo != null
        GeneratedDaoInfoHolder.allDaos.add(DaoInfo(packageName, daoClassName, containingFile, isPrimaryKey))

        val innerClass = buildClass(
            daoClassName,
            entityName,
        )

        val builder = FileSpec.builder("io.kotgres.orm.generated.dao", daoClassName)

        builder.indent(INDENTATION).addType(innerClass)

        // TODO not all imports may be needed, could be optimised
        TypeResolver.getSingleton().getAllMappers().forEach {
            builder.addImport(it.qualifiedName!!, "")
        }

        EXTRA_DATA_TYPES.forEach {
            builder.addImport(it.qualifiedName!!, "")
        }

        EXTRA_EXCEPTION_TYPES.forEach {
            builder.addImport(it.qualifiedName!!, "")
        }

        builder.addImport(KotgresNoEmptyConstructorException::class.java, "")

        fieldsInfo.filter { it.isEnumType }.forEach {
            builder.addImport(it.type.toClassName(), "")
        }

        return builder
    }

    private fun buildClass(
        daoClassName: String,
        entityName: String,
    ): TypeSpec {
        val tableName = getTableName(entityName)

//        logEntityInfo(tableName)

        val daoBuilder = buildDao(daoClassName, entityName, tableName)

        return daoBuilder.build()
    }

    @OptIn(KspExperimental::class)
    private fun getTableName(
        entityName: String,
    ): String {
        val tableAnnotation = entityClassDeclaration.getAnnotationsByType(Table::class).toList().first()
        return if (tableAnnotation.name != "") tableAnnotation.name else entityName.toSnakeCase()
    }

    private fun logEntityInfo(
        tableName: String,
    ) {
        logger.logging("")
        logger.warn("CREATE TABLE $tableName")
        fieldsInfo.forEach {
            logger.warn(
                "${it.columnName}: ${it.postgresType} (isPrimaryKey: ${it.isPrimaryKey}, " +
                        "isGenerated: ${it.isGenerated}, isUnique: ${it.isUnique}, hasEnumAnnotation: ${it.hasEnumAnnotation}, " +
                        "isEnumType: ${it.isEnumType}, isNullable: ${it.isNullable})",
            )
        }
        logger.warn("")
    }

    private fun buildMapResultFunction(): FunSpec {
        val mapResultFunctionBuilder = FunSpec.builder("mapQueryResult")
            .addModifiers(KModifier.OVERRIDE)
            .addParameter(
                ParameterSpec.builder("result", ResultSet::class).build(),
            )
            .returns(entityClassDeclaration.toClassName())

        mapQueryResultReflection(mapResultFunctionBuilder)
        return mapResultFunctionBuilder.build()
    }

    private fun buildGetNewInstanceFunction(entityName: String, daoName: String): FunSpec {
        return FunSpec.builder("getNewInstance")
            .addModifiers(KModifier.OVERRIDE)
            .returns(AbstractDao::class.asClassName().parameterizedBy(entityClassDeclaration.toClassName()))
            .addStatement("return ${daoName}(pool)")
            .build()
    }

    private fun mapQueryResultReflection(
        mapResultFunctionBuilder: FunSpec.Builder,
    ) {
        val className = entityClassDeclaration.simpleName.asString()

        // user constructor generated by no-arg
        mapResultFunctionBuilder.addStatement("val entity = emptyConstructor.newInstance() as $className")

        fieldsInfo.forEach {
            if (it.isEnumType) {
                // TODO add nullable here
                mapResultFunctionBuilder.addStatement("setFieldEnum<${it.type}>(\"${it.columnName}\", result, entity)")
            } else {
                val postFix = if (it.isNullable) "Nullable" else ""
                // TODO the as here is not clean but its because of the columnNameToMapper generic BaseMapper type, any way around?
                mapResultFunctionBuilder.addStatement(
                    "setField$postFix(\"${it.columnName}\", result, entity)",
                )
            }
        }
        mapResultFunctionBuilder.addStatement("return entity")
    }

    private fun buildDao(
        daoClassName: String,
        entityName: String,
        tableName: String,
    ): TypeSpec.Builder {
        val daoBuilder = TypeSpec.classBuilder(daoClassName)

        val className = entityClassDeclaration.simpleName.asString()

        daoBuilder.addKdoc("DAO for $className generated by Kotgres\nDo not manually change!")

        daoBuilder.addAnnotation(
            AnnotationSpec.builder(Deprecated::class)
                .addMember("message = \"Use DaoManager.getPrimaryKeyDao or DaoManager.getNoPrimaryKeyDao instead\"")
                .build()
        )

        addSuperClass(daoBuilder)

        addConstructor(daoBuilder)

        addCachedProperties(daoBuilder, className)

        if (primaryKeyInfo != null) {
            addPrimaryKeyDaoOverridedProperties(primaryKeyInfo, daoBuilder)
        }

        addAbstractDaoOverridesProperties(tableName, entityName, daoBuilder)

        daoBuilder.addProperty(
            PropertySpec.builder("emptyConstructor", Constructor::class.asClassName().parameterizedBy(STAR))
                .initializer(
                    """
                        javaClass.constructors
                        .firstOrNull { it.parameterCount == 0 } ?: throw KotgresNoEmptyConstructorException(className)
                    """.trimIndent(),
                )
                .addModifiers(KModifier.PRIVATE)
                .build(),
        )

        addInitBlock(daoBuilder)

        addAbstractDaoOverridesFunctions(daoBuilder, entityName, daoClassName)

        if (primaryKeyInfo != null) {
            addPrimaryKeyDaoOverridedFunctions(daoBuilder)
        }

//        addUniqueColumnFilters(daoBuilder)

        return daoBuilder
    }

    private fun addInitBlock(daoBuilder: TypeSpec.Builder) {
        val codeBlock = CodeBlock.builder()
            .beginControlFlow("for (fieldInfo in allFields.values)")
            .addStatement("fieldInfo.declaredField.isAccessible = true")
            .endControlFlow()

        if (primaryKeyInfo != null) {
            codeBlock.addStatement("primaryKeyFieldInfo.declaredField.isAccessible = true")
        }

        daoBuilder.addInitializerBlock(codeBlock.build())
    }

    private fun addCachedProperties(daoBuilder: TypeSpec.Builder, className: String) {
        daoBuilder.addProperty(
            PropertySpec.builder("javaClass", Class::class.asClassName().parameterizedBy(STAR))
                .initializer("$className::class.java")
                .addModifiers(KModifier.PRIVATE)
                .build(),
        )
    }

    private fun addAbstractDaoOverridesProperties(
        tableName: String,
        entityName: String,
        daoBuilder: TypeSpec.Builder,
    ) {
        daoBuilder.addProperty(
            PropertySpec.builder("tableName", String::class)
                .initializer("\"${tableName}\"")
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )

        daoBuilder.addProperty(
            PropertySpec.builder("className", String::class)
                .initializer("\"${entityName}\"")
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )

        val allFieldsInitializerCodeBlock = CodeBlock.builder()
            .addStatement("mapOf(")

        fieldsInfo.forEach {
            val initStatement = if (it.isPrimaryKey) {
                "primaryKeyFieldInfo"
            } else {
                propertyInfoToDaoColumnInfo(it)
            }
            allFieldsInitializerCodeBlock.addStatement(
                """
                    "${it.columnName}" to $initStatement,
                """.trimIndent(),
            )
        }

        allFieldsInitializerCodeBlock.addStatement(")")

        daoBuilder.addProperty(
            PropertySpec.builder(
                "allFields",
                Map::class.asClassName().parameterizedBy(
                    String::class.asClassName(),
                    DaoColumnInfo::class.asClassName()
                        .parameterizedBy(entityClassDeclaration.toClassName(), STAR),
                ),
            )
                .initializer(allFieldsInitializerCodeBlock.build())
                .addModifiers(KModifier.OVERRIDE)
                .build(),
        )
    }

    private fun addAbstractDaoOverridesFunctions(
        daoBuilder: TypeSpec.Builder,
        entityName: String,
        daoClassName: String,
    ) {
        val mapResultFunctionBuilder = buildMapResultFunction()
        val getNewInstanceFunction = buildGetNewInstanceFunction(entityName, daoClassName)
        daoBuilder.addFunction(mapResultFunctionBuilder)
        daoBuilder.addFunction(getNewInstanceFunction)
    }

//    private fun addUniqueColumnFilters(daoBuilder: TypeSpec.Builder) {
//        // if a user renames a field their code will break (fair enough)
//        fieldsInfo
//            .filter { it.isUnique }
//            .forEach { fieldInfo ->
//                val variableName = fieldInfo.fieldName
//                daoBuilder.addFunction(
//                    FunSpec.builder("getBy${variableName.replaceFirstChar { c -> c.uppercase() }}")
//                        .addParameter(variableName, Any::class)
//                        .returns(entityClassDeclaration.toClassName().copy(nullable = true))
//                        .addStatement("""return getByUniqueColumn("${fieldInfo.columnName}", $variableName)""")
//                        .build(),
//                )
//                daoBuilder.addFunction(
//                    FunSpec.builder("deleteBy${variableName.replaceFirstChar { c -> c.uppercase() }}")
//                        .addParameter(variableName, Any::class)
//                        .returns(Boolean::class)
//                        .addCode("""return (deleteByColumnValue("${fieldInfo.columnName}", $variableName) == 1)""")
//                        .build(),
//                )
//            }
//    }

    private fun addSuperClass(
        daoBuilder: TypeSpec.Builder,
    ) {
        if (primaryKeyInfo != null) {
            daoBuilder.superclass(
                PrimaryKeyDao::class.asClassName()
                    .parameterizedBy(entityClassDeclaration.toClassName(), primaryKeyInfo.type.toClassName()),
            )
        } else {
            daoBuilder.superclass(
                NoPrimaryKeyDao::class.asClassName().parameterizedBy(entityClassDeclaration.toClassName()),
            )
        }
    }

    private fun addConstructor(daoBuilder: TypeSpec.Builder) {
        daoBuilder.addSuperclassConstructorParameter("conn")
            .primaryConstructor(
                FunSpec.constructorBuilder()
                    .addParameter("conn", AbstractKotgresConnectionPool::class)
                    .build(),
            )
    }

    private fun propertyInfoToDaoColumnInfo(info: PropertyInfo): String {
        val mapperStatement = when (val mapperInfo = info.mapperInfo) {
            is MapperInfo.BuiltInMapperInfo -> {
                val mapperClassName = mapperInfo.kClass.simpleName

                val isCustomMapper = CustomMapper::class.java.isAssignableFrom(mapperInfo.kClass.java)
                if (isCustomMapper) {
                    "${mapperClassName}()"
                } else {
                    "${mapperClassName}(${info.isNullable})"
                }
            }

            is MapperInfo.CustomMapperInfo -> {
                "${mapperInfo.className}()"
            }

            else -> throw KotgresInternalException("Unknown MapperInfo type for ${info.mapperInfo}")
        }

        return """ 
            DaoColumnInfo(
                "${info.columnName}",
                { e -> e.${info.fieldName} },
                ${info.isGenerated},
                ${info.allowUpdates},
                javaClass.getDeclaredField("${info.fieldName}"),
                $mapperStatement,
            )
        """.trimIndent()
    }

    private fun addPrimaryKeyDaoOverridedProperties(primaryKeyInfo: PropertyInfo, daoBuilder: TypeSpec.Builder) {
        daoBuilder
            .addProperty(
                PropertySpec.builder(
                    "primaryKeyFieldInfo",
                    DaoColumnInfo::class.asClassName()
                        .parameterizedBy(entityClassDeclaration.toClassName(), primaryKeyInfo.type.toClassName()),
                )
                    .initializer(propertyInfoToDaoColumnInfo(primaryKeyInfo))
                    .addModifiers(KModifier.OVERRIDE)
                    .build(),
            )
    }

    private fun addPrimaryKeyDaoOverridedFunctions(daoBuilder: TypeSpec.Builder) {
        daoBuilder
            .addFunction(
                buildGetPrimaryKeyValueFunction().build(),
            )
    }

    // TODO we should handle better nullable pkeys?
    private fun buildGetPrimaryKeyValueFunction() = FunSpec.builder("getPrimaryKeyValue")
        .addModifiers(KModifier.OVERRIDE)
        .addParameter("entity", entityClassDeclaration.toClassName())
        .returns(primaryKeyInfo!!.type.toClassName())
        .addStatement("return entity.${primaryKeyInfo!!.fieldName} ?: throw KotgresCantUpdateNullPrimaryKeyEntityException()")

}
