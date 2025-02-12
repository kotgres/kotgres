package io.kotgres.orm.connection

data class KotgresConnectionPoolConfig(
    val host: String,
    val databaseName: String = "postgres",
    val port: Int = 5432,
    val username: String = "postgres",
    val password: String = "",
)
