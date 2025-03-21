package io.kotgres.orm.types

import io.kotgres.orm.exceptions.entity.KotgresNoMapperException
import io.kotgres.orm.exceptions.internal.KotgresInternalException
import io.kotgres.orm.types.base.AbstractMapper
import io.kotgres.orm.types.custom.EnumMapper
import io.kotgres.orm.types.custom.StringEnumMapper
import io.kotgres.orm.types.custom.json.GuavaMapper
import io.kotgres.orm.types.custom.json.JacksonMapper
import io.kotgres.orm.types.custom.json.JsonJavaMapper
import io.kotgres.orm.types.custom.json.KotlinxSerializationMapper
import io.kotgres.orm.types.primitive.BooleanMapper
import io.kotgres.orm.types.primitive.DoubleMapper
import io.kotgres.orm.types.primitive.FloatMapper
import io.kotgres.orm.types.primitive.IntMapper
import io.kotgres.orm.types.primitive.LongMapper
import io.kotgres.orm.types.primitive.ShortMapper
import io.kotgres.orm.types.primitive.StringMapper
import io.kotgres.orm.types.primitive.arrays.IntArrayListMapper
import io.kotgres.orm.types.primitive.arrays.StringArrayListMapper
import io.kotgres.orm.types.psql.builtin.BigDecimalMapper
import io.kotgres.orm.types.psql.builtin.DateMapper
import io.kotgres.orm.types.psql.builtin.LocalDateTimeMapper
import io.kotgres.orm.types.psql.builtin.OffsetDateTimeMapper
import io.kotgres.orm.types.psql.builtin.TimeMapper
import io.kotgres.orm.types.psql.extensions.UuidMapper
import kotlin.reflect.KClass

private const val STRING_TYPE = "kotlin.String"

private val BUILT_IN_MAPPERS = mutableListOf(
    // primitive
    BooleanMapper::class,
    DoubleMapper::class,
    FloatMapper::class,
    IntMapper::class,
    LongMapper::class,
    ShortMapper::class,
    StringMapper::class,
    // built-in
    BigDecimalMapper::class,
    DateMapper::class,
    LocalDateTimeMapper::class,
    OffsetDateTimeMapper::class,
//        SqlXmlMapper::class, // TODO enable and test, maybe
    TimeMapper::class,
    // extensions
    UuidMapper::class,
    // custom
    EnumMapper::class,
    StringEnumMapper::class,
    // json
//    JsonJavaMapper::class,
//    GuavaMapper::class,
//    JacksonMapper::class,
//    KotlinxSerializationMapper::class,
)

class TypeResolver private constructor() {

    /**
     * PRIVATE FIELDS
     */

    private var mappersNameToKclass: MutableMap<String, KClass<out AbstractMapper<*>>> = mutableMapOf()
    private var kotlinTypeToMapperClass: MutableMap<String, KClass<out AbstractMapper<*>>> = mutableMapOf()
    private var postgresTypeToKotlinType: MutableMap<String, MutableList<String>> = mutableMapOf()
    //    private val postgresTypeToMapperClass: Map<String, KClass<*>>

    /**
     * INIT METHOD
     */

    init {
        registerMapperList(BUILT_IN_MAPPERS)
    }

    /**
     * PUBLIC METHODS
     */

    fun getAllMappers(): List<KClass<out AbstractMapper<*>>> {
        return mappersNameToKclass.values.toList()
    }

    fun getMapperKClass(
        typeName: String,
        isNullable: Boolean,
        isEnumType: Boolean,
        hasEnumAnnotation: Boolean,
        columnName: String?,
        listParameterType: String? = null
    ): KClass<out AbstractMapper<*>> {
        val mapper = if (isEnumType) {
            EnumMapper()::class
        } else if (typeName == STRING_TYPE && hasEnumAnnotation) {
            StringEnumMapper()::class
        } else if (listParameterType != null) {
            if (listParameterType == "kotlin.Int") {
                IntArrayListMapper(isNullable)::class
            } else if (listParameterType == "kotlin.String") {
                StringArrayListMapper(isNullable)::class
            } else {
                throw KotgresInternalException("Unsupported List<${listParameterType}>")
            }
        } else {
            resolveFromName(typeName) ?: throw KotgresNoMapperException(columnName, typeName)
        }

        return mapper
    }

    fun getJavaType(postgresTypeName: String): List<String>? {
        return postgresTypeToKotlinType[postgresTypeName]
    }

    /**
     * PRIVATE METHODS
     */

    private fun registerMapperList(mapperKclassList: List<KClass<out AbstractMapper<*>>>) {
        mapperKclassList.forEach {
            registerMapper(it)
        }
    }

    private fun registerMapper(mapperKclass: KClass<out AbstractMapper<*>>) {
        val mapper = callMapperConstructor(mapperKclass)
        val mapperClassName = mapper::class.qualifiedName!!

        if (mappersNameToKclass[mapperClassName] != null) {
            println("Skipping mapper $mapperClassName since it's already registered")
            return
        }

        mappersNameToKclass[mapperClassName] = mapperKclass

        val kotlinClassName = mapper.kotlinClassName

        if (kotlinClassName == null) {
            println("Skipping full mapper registration for $mapperClassName since it does not have a kotlinClassName")
            return
        }

        if (kotlinTypeToMapperClass[kotlinClassName] != null) {
            throw KotgresInternalException("Multiple mappers for $kotlinClassName")
        }
        kotlinTypeToMapperClass[kotlinClassName] = mapperKclass
        mapper.postgresTypes?.forEach { pgType ->
            postgresTypeToKotlinType.getOrPut(pgType) { mutableListOf() }.add(kotlinClassName)
        }

        //            mapper.postgresTypes?.forEach { pgType ->
        //                if (newPostgresTypeToMapperClass[pgType] != null) {
        //                    throw KotgresInternalException("Multiple mappers for $pgType")
        //                }
        //                newPostgresTypeToMapperClass[pgType] = it
        //            }
    }

    private fun getMapperKlassFromName(typeName: String): KClass<out AbstractMapper<*>>? {
        val finalTypeName = if (typeName == "java.util.Arrays.ArrayList") {
            "kotlin.collections.List"
        } else {
            typeName
        }

        return kotlinTypeToMapperClass[finalTypeName]
    }

    private fun resolveFromName(typeName: String): KClass<out AbstractMapper<*>>? {
        return getMapperKlassFromName(typeName)
    }

    companion object {
        // Manual singleton because kotlin does not allow visibility modifiers in objects :<
        private val instance: TypeResolver = TypeResolver()
        internal fun getSingleton() = instance

        // TODO find a cleaner way... Using try catch since I cant find a way to tell AbstractMapper and SimpleMapper apart
        // TODO also this is not performant due to extra constant exceptions
        fun callMapperConstructor(mapperKclass: KClass<out AbstractMapper<*>>) = try {
            (mapperKclass.constructors.first().call(false))
        } catch (e: Exception) {
            (mapperKclass.constructors.first().call())
        }
    }
}
