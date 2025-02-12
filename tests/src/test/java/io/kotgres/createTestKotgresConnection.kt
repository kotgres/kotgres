package io.kotgres

import io.kotgres.orm.connection.AbstractKotgresConnectionPool
import io.kotgres.orm.connection.KotgresConnectionPoolConfig
import io.kotgres.orm.connection.KotgresConnectionPool

fun createTestKotgresConnection(): AbstractKotgresConnectionPool {
    return KotgresConnectionPool.build(
        KotgresConnectionPoolConfig(
            "0.0.0.0",
            "kotgres",
            54329,
            "kotgres",
            "kotgres123",
        ),
    )
}
