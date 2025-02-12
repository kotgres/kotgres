package io.kotgres.orm.internal.processors

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.getAnnotationsByType
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.google.devtools.ksp.symbol.Nullability
import com.google.devtools.ksp.validate
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.writeTo
import io.kotgres.orm.annotations.Column
import io.kotgres.orm.annotations.Enum
import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table
import io.kotgres.orm.annotations.Unique
import io.kotgres.orm.annotations.UseCustomMapper
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
        val symbols = resolver.getSymbolsWithAnnotation(Table::class.qualifiedName.toString())

        val ret = symbols.filter { !it.validate() }.toList()

        symbols
            .filter { it is KSClassDeclaration && it.validate() }
            .forEach {
                it.accept(TableProcessirVisitor(), Unit)
            }

        // Also create DaoManager
        DaoManagerBuilder(codeGenerator, logger).build()

        return ret
    }

    inner class TableProcessirVisitor : KSVisitorVoid() {

        override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
            classDeclaration.primaryConstructor!!.accept(this, data)

            val entityInfo = extractEntityInfo(classDeclaration)

            val builder = DaoBuilder(logger, classDeclaration, entityInfo).buildDao(classDeclaration.containingFile!!)

            val fileSpec: FileSpec = builder.build()

            fileSpec.writeTo(
                codeGenerator = codeGenerator,
                aggregating = true,
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
                    it.annotationType.resolve().toClassName() == UseCustomMapper::class.asClassName()
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
