package io.kotgres.orm.connection

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.kotgres.orm.internal.ApLogger
import java.sql.Connection
import java.util.Properties

const val DEFAULT_APPLICATION_NAME = "KOTGRES"

class KotgresConnectionPool private constructor(private val hikariDataSource: HikariDataSource) : AbstractKotgresConnectionPool() {

    override fun getConnection(): Connection {
        return hikariDataSource.connection
    }

    override fun close() {
        hikariDataSource.close()
    }

    companion object {

        fun build(
            kotgresConnectionPoolConfig: KotgresConnectionPoolConfig,
            extraProperties: Properties = Properties(),
            applicationName: String = DEFAULT_APPLICATION_NAME,
        ): AbstractKotgresConnectionPool {
            ApLogger.debug("Initialising connection pool...")
            val config = HikariConfig()
            config.jdbcUrl = buildJdbcUrl(kotgresConnectionPoolConfig)
            config.username = kotgresConnectionPoolConfig.username
            config.password = kotgresConnectionPoolConfig.password

            config.addDataSourceProperty("ApplicationName", applicationName)

            extraProperties.forEach { key, value ->
                config.addDataSourceProperty(key.toString(), value)
            }

            return KotgresConnectionPool(HikariDataSource(config))
        }

        private fun buildJdbcUrl(config: KotgresConnectionPoolConfig): String {
            return "jdbc:postgresql://${config.host}:${config.port}/${config.databaseName}?selectMethod=direct"
        }

    }
}
