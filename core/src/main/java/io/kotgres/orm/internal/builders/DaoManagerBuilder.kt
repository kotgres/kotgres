package io.kotgres.orm.internal.builders

import com.google.devtools.ksp.KspExperimental
import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.ksp.writeTo
import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.dao.NoPrimaryKeyDao
import io.kotgres.orm.dao.PrimaryKeyDao
import io.kotgres.orm.exceptions.base.KotgresException
import io.kotgres.orm.exceptions.dao.KotgresClassHasNoNameException
import io.kotgres.orm.exceptions.dao.KotgresDaoNotFoundException
import io.kotgres.orm.internal.processors.model.BuilderConstants
import io.kotgres.orm.internal.utils.BuilderUtils
import io.kotgres.orm.internal.utils.DaoInfo
import io.kotgres.orm.internal.utils.GeneratedDaoInfoHolder
import kotlin.reflect.KClass

private const val name = "DaoManager"


// constants
private val EXTRA_EXCEPTION_TYPES =
    listOf(
        KotgresDaoNotFoundException::class,
        KotgresClassHasNoNameException::class,
        KotgresException::class,
    )

internal class DaoManagerBuilder(
    val codeGenerator: CodeGenerator,
    val logger: KSPLogger,
) {
    private val templateTypeEntity = TypeVariableName.Companion.invoke("E", Any::class.asClassName())
    private val templateTypeEntityPrimaryKey = TypeVariableName.Companion.invoke("I", Any::class.asClassName())

    @OptIn(KspExperimental::class)
    fun build(resolver: Resolver) {
        val isTest = resolver.getAllFiles().any { it.filePath.contains("/test/") }
        logger.warn("Building DaoManager for ${if (isTest) "test" else "main"}")

// TODO add map to optimise
//        val propBuilder = PropertySpec.builder(
//            "primaryKeyDaos",
//            Map::class.asClassName().parameterizedBy(
//                String::class.asClassName(),
//                KClass::class.asClassName().parameterizedBy(
//                    STAR,
//                ),
//            ),
//        ).addModifiers(KModifier.PRIVATE)

//        val codeBlock = CodeBlock.builder()
//        codeBlock.addStatement("mapOf(")
//        DaoInfoallCreated.forEach { (packageName, className) ->
//            codeBlock.addStatement("""$INDENTATION"$className" to""")
//            codeBlock.addStatement("""${INDENTATION.repeat(2)}$className::class,""")
//        }
//        codeBlock.addStatement(")")
//        propBuilder.initializer(codeBlock.build())

        val generateDaoInfosFromOtherSourceSets = getGeneratedDaosFromOtherSourceSets(resolver)

        val allDaos =  (GeneratedDaoInfoHolder.allDaos + generateDaoInfosFromOtherSourceSets)

        val primaryKeyDaos = allDaos.filter { it.isPrimaryKey }
        val nonPrimaryKeyDaos = allDaos.filter { !it.isPrimaryKey }

        val pkByStringFunction = getPkDaoByStringFunction(primaryKeyDaos)
        val pkByKclassFunctionKotlin = getPkDaoByKclassFunctionKotlin()
        val pkByKclassFunctionJava = getPkDaoByKclassFunctionJava()

        val noPkByStringFunction = getNoPkDaoByStringFunction(nonPrimaryKeyDaos)
        val noPkByKclassFunctionKotlin = getNoPkDaoByKclassFunctionKotlin()
        val noPkByKclassFunctionJava = getNoPkDaoByKclassFunctionJava()

        val allPrimaryKeyDaosProp = getAllPrimaryKeyDaosProp(primaryKeyDaos)
        val allNoPrimaryKeyDaosProp = getAllNoPrimaryKeyDaosProp(nonPrimaryKeyDaos)

        buildFile(
            allDaos,
            allPrimaryKeyDaosProp,
            allNoPrimaryKeyDaosProp,
            pkByStringFunction,
            pkByKclassFunctionKotlin,
            pkByKclassFunctionJava,
            noPkByStringFunction,
            noPkByKclassFunctionKotlin,
            noPkByKclassFunctionJava
        )

        return
    }

    @OptIn(KspExperimental::class)
    private fun getGeneratedDaosFromOtherSourceSets(resolver: Resolver): List<DaoInfo> {
        val generatedDaos: List<KSClassDeclaration> =
            resolver.getDeclarationsFromPackage("io.kotgres.orm.generated.dao")
                .filterIsInstance<KSClassDeclaration>()
                .filter { it.classKind == ClassKind.CLASS }
                .distinctBy { it.qualifiedName?.asString() }
                .toList()

        logger.warn("Found ${generatedDaos.size} generated valid daos")

        val generateDaoInfos = generatedDaos.map { declaration ->
            val isNonPrimaryKey = declaration.superTypes
                .map { it.resolve().declaration.qualifiedName?.asString() }
                .any { it?.contains("NoPrimaryKeyDao") == true }

            DaoInfo(
                packageName = declaration.packageName.asString(),
                daoClassName = declaration.simpleName.asString(),
                // we are missing the ksFile from the other source sets, but it should be okay
                ksFile = null,
                isPrimaryKey = !isNonPrimaryKey
            )
        }

        logger.warn("Found Generated Daos: ${generateDaoInfos.size}")
        return generateDaoInfos
    }

    private fun buildFile(
        allDaos: List<DaoInfo>,
        allPrimaryKeyDaosProp: PropertySpec,
        allNoPrimaryKeyDaosProp: PropertySpec,
        byStringFunction: FunSpec,
        byKclassFunctionKotlin: FunSpec,
        byKclassFunctionJava: FunSpec,
        noPkByStringFunction: FunSpec,
        noPkByKclassFunctionKotlin: FunSpec,
        noPkByKclassFunctionJava: FunSpec,
    ) {
        val objectBuilder = TypeSpec.objectBuilder(name)
        objectBuilder.addFunction(byStringFunction)
        objectBuilder.addFunction(byKclassFunctionKotlin)
        objectBuilder.addFunction(byKclassFunctionJava)
        objectBuilder.addFunction(noPkByStringFunction)
        objectBuilder.addFunction(noPkByKclassFunctionKotlin)
        objectBuilder.addFunction(noPkByKclassFunctionJava)
        objectBuilder.addProperty(allPrimaryKeyDaosProp)
        objectBuilder.addProperty(allNoPrimaryKeyDaosProp)
        //        objectBuilder.addProperty(propBuilder.build())

        val fileBuilder = FileSpec.builder("io.kotgres.orm.manager", name)
        fileBuilder.addType(objectBuilder.build())

        allDaos.forEach { (packageName, className) ->
            fileBuilder.addImport("io.kotgres.orm.generated.dao", className)
        }

        EXTRA_EXCEPTION_TYPES.forEach {
            fileBuilder.addImport(it.qualifiedName!!, "")
        }

        logger.warn("Building DaoManager file " + allDaos.size + " DAOs")

        val dependencies = Dependencies(
            aggregating = true,
            sources = (allDaos.mapNotNull { it.ksFile }).toTypedArray()
        )

        try {
            fileBuilder.indent(BuilderConstants.INDENTATION).build().writeTo(
                codeGenerator = codeGenerator,
                dependencies = dependencies
            )
        } catch (e: Exception) {
            logger.info("Failed to write DaoManager file" + e.message)
        }

    }

    private fun getPkDaoByKclassFunctionJava(): FunSpec {
        return FunSpec.builder("getPrimaryKeyDao")
            .addModifiers(KModifier.PUBLIC)
            .addTypeVariable(templateTypeEntity)
            .addTypeVariable(templateTypeEntityPrimaryKey)
            .addParameter("javaClass", Class::class.asTypeName().parameterizedBy(templateTypeEntity))
            .addParameter(
                ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build()
            )
            .returns(
                PrimaryKeyDao::class.asClassName().parameterizedBy(templateTypeEntity, templateTypeEntityPrimaryKey),
            )
            .addCode(
                """
            val name = javaClass.simpleName ?: throw KotgresClassHasNoNameException("java")
            return getPrimaryKeyDaoByString(name, conn)
            """.trimIndent()
            )
            .build()
    }

    private fun getNoPkDaoByKclassFunctionJava(): FunSpec {
        return FunSpec.builder("getNoPrimaryKeyDao")
            .addModifiers(KModifier.PUBLIC)
            .addTypeVariable(templateTypeEntity)
            .addParameter("javaClass", Class::class.asTypeName().parameterizedBy(templateTypeEntity))
            .addParameter(
                ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build()
            )
            .returns(
                NoPrimaryKeyDao::class.asClassName().parameterizedBy(templateTypeEntity),
            )
            .addCode(
                """
            val name = javaClass.simpleName ?: throw KotgresClassHasNoNameException("java")
            return getNoPrimaryKeyDaoByString(name, conn)
            """.trimIndent()
            )
            .build()
    }

    private fun getPkDaoByKclassFunctionKotlin(): FunSpec {
        return FunSpec.builder("getPrimaryKeyDao")
            .addModifiers(KModifier.PUBLIC)
            .addTypeVariable(templateTypeEntity)
            .addTypeVariable(templateTypeEntityPrimaryKey)
            .addParameter("klass", KClass::class.asTypeName().parameterizedBy(templateTypeEntity))
            .addParameter(
                ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build()
            )
            .returns(
                PrimaryKeyDao::class.asClassName().parameterizedBy(templateTypeEntity, templateTypeEntityPrimaryKey),
            )
            .addCode(
                """
            val name = klass.simpleName ?: throw KotgresClassHasNoNameException("kotlin")
            return getPrimaryKeyDaoByString(name, conn)
            """.trimIndent()
            )
            .build()
    }

    private fun getNoPkDaoByKclassFunctionKotlin(): FunSpec {
        return FunSpec.builder("getNoPrimaryKeyDao")
            .addModifiers(KModifier.PUBLIC)
            .addTypeVariable(templateTypeEntity)
            .addParameter("klass", KClass::class.asTypeName().parameterizedBy(templateTypeEntity))
            .addParameter(
                ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build()
            )
            .returns(
                NoPrimaryKeyDao::class.asClassName().parameterizedBy(templateTypeEntity)
            )
            .addCode(
                """
            val name = klass.simpleName ?: throw KotgresClassHasNoNameException("kotlin")
            return getNoPrimaryKeyDaoByString(name, conn)
            """.trimIndent()
            )
            .build()
    }

    private fun getPkDaoByStringFunction(primaryKeyDaos: List<DaoInfo>): FunSpec {
        val functionGetDao = FunSpec.builder("getPrimaryKeyDaoByString")
        functionGetDao.addModifiers(KModifier.PRIVATE)
        functionGetDao.addTypeVariable(templateTypeEntity)
        functionGetDao.addTypeVariable(templateTypeEntityPrimaryKey)
        functionGetDao.addParameter(ParameterSpec.Companion.builder("klass", String::class.asClassName()).build())
        functionGetDao.addParameter(
            ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build()
        )
        // add .copy(nullable = true) at the end to make it nullable
        functionGetDao.returns(
            PrimaryKeyDao::class.asClassName().parameterizedBy(templateTypeEntity, templateTypeEntityPrimaryKey),
        )

        functionGetDao.beginControlFlow("return when (klass.lowercase())")
        primaryKeyDaos.forEach { (packageName, daoClassName) ->
            val daoReturn = "$daoClassName(conn) as PrimaryKeyDao<E, I>"
            functionGetDao.addStatement(
                """${BuilderConstants.INDENTATION}"${
                    BuilderUtils.daoNameToEntityName(daoClassName).lowercase()
                }" -> $daoReturn"""
            )
        }
        functionGetDao.addStatement("${BuilderConstants.INDENTATION}else ->")
        functionGetDao.addStatement("${BuilderConstants.INDENTATION.repeat(2)}throw KotgresDaoNotFoundException(klass, allPrimaryKeyDaos)")
        functionGetDao.endControlFlow()

        return functionGetDao.build()
    }

    private fun getNoPkDaoByStringFunction(nonPrimaryKeyDaos: List<DaoInfo>): FunSpec {
        val functionGetDao = FunSpec.builder("getNoPrimaryKeyDaoByString")
        functionGetDao.addModifiers(KModifier.PRIVATE)
        functionGetDao.addTypeVariable(templateTypeEntity)
        functionGetDao.addParameter(ParameterSpec.Companion.builder("klass", String::class.asClassName()).build())
        functionGetDao.addParameter(
            ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build()
        )
        // add .copy(nullable = true) at the end to make it nullable
        functionGetDao.returns(
            NoPrimaryKeyDao::class.asClassName().parameterizedBy(templateTypeEntity),
        )

        functionGetDao.beginControlFlow("return when (klass.lowercase())")

        nonPrimaryKeyDaos.forEach { (packageName, daoClassName) ->
            val daoReturn = "$daoClassName(conn) as NoPrimaryKeyDao<E>"
            functionGetDao.addStatement(
                """${BuilderConstants.INDENTATION}"${
                    BuilderUtils.daoNameToEntityName(daoClassName).lowercase()
                }" -> $daoReturn"""
            )
        }
        functionGetDao.addStatement("${BuilderConstants.INDENTATION}else ->")
        functionGetDao.addStatement("${BuilderConstants.INDENTATION.repeat(2)}throw KotgresDaoNotFoundException(klass, allNoPrimaryKeyDaos)")
        functionGetDao.endControlFlow()

        return functionGetDao.build()
    }

    private fun getAllPrimaryKeyDaosProp(primaryKeyDaos: List<DaoInfo>): PropertySpec {
        val codeblock = CodeBlock.builder().addStatement("listOf(")
        primaryKeyDaos.forEach { (packageName, className) ->
            codeblock.addStatement("${BuilderConstants.INDENTATION}\"$className\",")
        }
        codeblock.addStatement(")")

        val prop =
            PropertySpec.Companion.builder(
                "allPrimaryKeyDaos",
                List::class.asClassName().parameterizedBy(String::class.asClassName())
            )
                .initializer(codeblock.build())
                .addModifiers(KModifier.PRIVATE)
                .build()
        return prop
    }

    private fun getAllNoPrimaryKeyDaosProp(nonPrimaryKeyDaos: List<DaoInfo>): PropertySpec {
        val codeblock = CodeBlock.builder().addStatement("listOf(")
        nonPrimaryKeyDaos.forEach { (packageName, className) ->
            codeblock.addStatement("${BuilderConstants.INDENTATION}\"$className\",")
        }
        codeblock.addStatement(")")

        val prop =
            PropertySpec.Companion.builder(
                "allNoPrimaryKeyDaos",
                List::class.asClassName().parameterizedBy(String::class.asClassName())
            )
                .initializer(codeblock.build())
                .addModifiers(KModifier.PRIVATE)
                .build()
        return prop
    }
}