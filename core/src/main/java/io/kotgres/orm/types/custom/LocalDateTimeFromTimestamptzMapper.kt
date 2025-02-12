//package io.kotgres.orm.types.custom
//
//import io.kotgres.orm.types.base.AbstractMapper
//import java.sql.PreparedStatement
//import java.sql.ResultSet
//import java.sql.Timestamp
//import java.time.LocalDateTime
//import java.time.ZoneOffset
//
//class LocalDateTimeFromTimestamptzMapper(nullable: Boolean = false) :
//    AbstractMapper<LocalDateTime>(LocalDateTime::class, nullable) {
//    override val postgresTypes: List<String>?
//        get() = listOf("timestamptz")
//
//    override fun fromSql(resultSet: ResultSet, position: Int): LocalDateTime? {
//        val timestamp = resultSet.getTimestamp(position)
//        return timestamp.toLocalDateTime()
//    }
//
//    override fun toSql(value: LocalDateTime, preparedStatement: PreparedStatement, position: Int, conn: Connection) {
//        preparedStatement.setTimestamp(position, Timestamp(value.toEpochSecond(ZoneOffset.UTC)))
//    }
//}
