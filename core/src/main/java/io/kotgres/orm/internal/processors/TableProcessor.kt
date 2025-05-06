package io.kotgres.orm.internal.processors

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.*
import com.google.devtools.ksp.symbol.*
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.kotgres.orm.annotations.*
import io.kotgres.orm.annotations.Enum
import io.kotgres.orm.exceptions.entity.KotgresPrimaryKeyException
import io.kotgres.orm.exceptions.internal.KotgresInternalException
import io.kotgres.orm.internal.builders.DaoBuilder
import io.kotgres.orm.internal.builders.DaoManagerBuilder
import io.kotgres.orm.internal.processors.model.EntityInfo
import io.kotgres.orm.internal.processors.model.MapperInfo
import io.kotgres.orm.internal.processors.model.PropertyInfo
import io.kotgres.orm.internal.utils.toSnakeCase
import io.kotgres.orm.types.TypeResolver

internal class TableProcessor(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
) : SymbolProcessor {

    // dependencies
    private val typeResolver = TypeResolver.getSingleton()

    override fun process(resolver: Resolver): List<KSAnnotated> {
        val allFiles = resolver.getAllFiles()
        val isTest = allFiles.any { it.filePath.contains("/test/") }

        logger.warn("Running DaoProcessor in ${if (isTest) "TEST" else "MAIN"} context")

        if (!resolver.getSymbolsWithAnnotation(Table::class.qualifiedName.toString(), inDepth = true).iterator().hasNext()) {
            return emptyList() // wait until symbols are available
        }

        val symbols = resolver.getSymbolsWithAnnotation(Table::class.qualifiedName.toString(), true)

        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                it.accept(TableProcessirVisitor(), Unit)
            }

        // Create DaoManager
        DaoManagerBuilder(codeGenerator, logger).build(resolver)

        return emptyList()
    }

    inner class TableProcessirVisitor : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)

            val entityInfo = extractEntityInfo(classDeclaration)

            val builder = DaoBuilder(logger, classDeclaration, entityInfo).buildDao(classDeclaration.containingFile!!)

            val fileSpec: FileSpec = builder.build()

            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                aggregating = false,
                originatingKSFiles = mutableListOf(classDeclaration.containingFile!!)
            )
        }

        private fun extractEntityInfo(classDeclaration: KSClassDeclaration): EntityInfo {
            var primaryKeyInfo: PropertyInfo? = null
            val foundProperties = mutableListOf<PropertyInfo>()

            classDeclaration.getAllProperties().forEach { property ->
                val propertyInfo = extractPropertyInfo(property)

                if (propertyInfo.isPrimaryKey) {
                    if (primaryKeyInfo != null) {
                        // TODO implement this
                        throw KotgresPrimaryKeyException("Tables with multi-field primary keys are not supported")
                    }
                    primaryKeyInfo = propertyInfo
                }

                foundProperties.add(propertyInfo)
            }

            return EntityInfo(foundProperties, primaryKeyInfo)
        }

        @OptIn(KspExperimental::class)
        private fun extractPropertyInfo(prop: KSPropertyDeclaration): PropertyInfo {
            val propType = prop.type.resolve()
            val typeName = propType.declaration.qualifiedName!!.asString()
            val generatedAnnotation = prop.getAnnotationsByType(Generated::class).firstOrNull()

            val propertyName = prop.simpleName.asString()
            val columnName = getColumnName(prop, propertyName)
            val isNullable = isNullable(prop)
            val isEnumType = isEnumType(propType)
            val hasEnumAnnotation = hasEnumAnnotation(prop)


            val mapperInfo = extractMapperKclass(
                prop,
                typeName,
                isNullable,
                isEnumType,
                hasEnumAnnotation,
                columnName
            )

            return PropertyInfo(
                columnName = columnName,
                fieldName = propertyName,
                type = prop.type.resolve(),
                isGenerated = generatedAnnotation != null,
                mapperInfo = mapperInfo,
                postgresType = typeResolver.getJavaType(typeName)?.joinToString(" or ") ?: "UNKNOWN",
                isPrimaryKey = isPrimaryKey(prop),
                allowUpdates = generatedAnnotation?.allowUpdates ?: true,
                isUnique = isUnique(prop),
                hasEnumAnnotation = hasEnumAnnotation,
                isEnumType = isEnumType,
                isNullable = isNullable,
            )
        }

        private fun extractMapperKclass(
            prop: KSPropertyDeclaration,
            typeName: String,
            isNullable: Boolean,
            isEnumType: Boolean,
            hasEnumAnnotation: Boolean,
            columnName: String
        ): MapperInfo {
            val res = prop.annotations
                .firstOrNull {
                    it.annotationType.resolve().toClassName() == CustomMapper::class.asClassName()
                }
                ?.arguments
                ?.first()
                ?.value
                ?.let { it as? KSType }
                ?.declaration
                ?.qualifiedName
                ?.asString()

            if (res != null) {
                return MapperInfo.CustomMapperInfo(res)
            }

            return MapperInfo.BuiltInMapperInfo(
                typeResolver.getMapperKClass(typeName, isNullable, isEnumType, hasEnumAnnotation, columnName)
            )
        }

        @OptIn(KspExperimental::class)
        private fun hasEnumAnnotation(prop: KSPropertyDeclaration): Boolean {
            val enumAnnotation = prop.getAnnotationsByType(Enum::class).firstOrNull()
            return enumAnnotation != null
        }

        @OptIn(KspExperimental::class)
        private fun isUnique(prop: KSPropertyDeclaration): Boolean {
            val uniqueAnnotation = prop.getAnnotationsByType(Unique::class).firstOrNull()
            return uniqueAnnotation != null
        }

        @OptIn(KspExperimental::class)
        private fun isPrimaryKey(prop: KSPropertyDeclaration): Boolean {
            val primaryKeyAnnotation = prop.getAnnotationsByType(PrimaryKey::class).firstOrNull()
            return primaryKeyAnnotation != null
        }

        private fun isEnumType(propType: KSType): Boolean {
            val classDeclaration = propType.declaration as KSClassDeclaration
            return classDeclaration.classKind == ClassKind.ENUM_CLASS
        }

        @OptIn(KspExperimental::class)
        private fun getColumnName(prop: KSPropertyDeclaration, propertyName: String): String {
            val columnAnnotation = prop.getAnnotationsByType(Column::class).firstOrNull()
            return columnAnnotation?.name ?: propertyName.toSnakeCase()
        }

        private fun isNullable(prop: KSPropertyDeclaration): Boolean {
            val isNullable = when (prop.type.resolve().nullability) {
                Nullability.NULLABLE -> true
                Nullability.NOT_NULL -> false
                else -> throw KotgresInternalException("Cannot tell if type is nullable or not")
            }
            return isNullable
        }
    }
}

class TableProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment,
    ): SymbolProcessor {
        return TableProcessor(environment.codeGenerator, environment.logger)
    }
}
