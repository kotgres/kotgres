package io.kotgres.orm.internal.builders

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.asTypeName
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
import kotlin.reflect.KClass

private const val name = "DaoManager"

// state
private var wasAlreadyInvoked = false

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

    fun build() {
        if (wasAlreadyInvoked) return

//        logger.error("Running DaoManager ${DaoInfocreatePkeyDaos.joinToString(", ")}")

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

        val pkByStringFunction = getPkDaoByStringFunction()
        val pkByKclassFunctionKotlin = getPkDaoByKclassFunctionKotlin()
        val pkByKclassFunctionJava = getPkDaoByKclassFunctionJava()

        val noPkByStringFunction = getNoPkDaoByStringFunction()
        val noPkByKclassFunctionKotlin = getNoPkDaoByKclassFunctionKotlin()
        val noPkByKclassFunctionJava = getNoPkDaoByKclassFunctionJava()

        val allPrimaryKeyDaosProp = getAllPrimaryKeyDaosProp()
        val allNoPrimaryKeyDaosProp = getAllNoPrimaryKeyDaosProp()

        buildFile(
            allPrimaryKeyDaosProp,
            allNoPrimaryKeyDaosProp,
            pkByStringFunction,
            pkByKclassFunctionKotlin,
            pkByKclassFunctionJava,
            noPkByStringFunction,
            noPkByKclassFunctionKotlin,
            noPkByKclassFunctionJava
        )

        wasAlreadyInvoked = true

        return
    }

    private fun buildFile(
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

        (DaoInfo.createPkeyDaos + DaoInfo.createNoPkeyDaos).forEach { (packageName, className) ->
            fileBuilder.addImport("io.kotgres.orm.dao", className)
        }

        EXTRA_EXCEPTION_TYPES.forEach {
            fileBuilder.addImport(it.qualifiedName!!, "")
        }

        fileBuilder.indent(BuilderConstants.INDENTATION).build().writeTo(
            codeGenerator = codeGenerator,
            aggregating = true,
            originatingKSFiles = DaoInfo.createPkeyDaosOriginatingKSFiles + DaoInfo.createNoPkeyDaosOriginatingKSFiles
        )
    }

    private fun getPkDaoByKclassFunctionJava(): FunSpec {
        return FunSpec.builder("getPrimaryKeyDao")
            .addModifiers(KModifier.PUBLIC)
            .addTypeVariable(templateTypeEntity)
            .addTypeVariable(templateTypeEntityPrimaryKey)
            .addParameter("javaClass", Class::class.asTypeName().parameterizedBy(templateTypeEntity))
            .addParameter(ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build())
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
            .addParameter(ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build())
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
            .addParameter(ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build())
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
            .addParameter(ParameterSpec.Companion.builder("conn", AbstractKotgresConnectionPool::class.asClassName()).build())
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

    private fun getPkDaoByStringFunction(): FunSpec {
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
        DaoInfo.createPkeyDaos.forEach { (packageName, daoClassName) ->
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

    private fun getNoPkDaoByStringFunction(): FunSpec {
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
        DaoInfo.createNoPkeyDaos.forEach { (packageName, daoClassName) ->
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

    private fun getAllPrimaryKeyDaosProp(): PropertySpec {
        val codeblock = CodeBlock.builder().addStatement("listOf(")
        DaoInfo.createPkeyDaos.forEach { (packageName, className) ->
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

    private fun getAllNoPrimaryKeyDaosProp(): PropertySpec {
        val codeblock = CodeBlock.builder().addStatement("listOf(")
        DaoInfo.createNoPkeyDaos.forEach { (packageName, className) ->
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