package io.kotgres.orm.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Table(
    val name: String = "",
)
