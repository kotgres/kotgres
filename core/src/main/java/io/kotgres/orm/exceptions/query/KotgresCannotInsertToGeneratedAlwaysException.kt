package io.kotgres.orm.exceptions.query

import io.kotgres.orm.exceptions.base.KotgresException
import org.postgresql.util.PSQLException

class KotgresCannotInsertToGeneratedAlwaysException(val psqlException: PSQLException, columnName: String) :
    KotgresException("Cannot insert a non-DEFAULT value into column $columnName because it is a GENERATED ALWAYS column.\nDid you miss adding @Generated to that field?\nOr maybe you want to override this behaviour by using OVERRIDING SYSTEM VALUE? (you can do it in dao.insertQuery using overridingSystemValue)")
