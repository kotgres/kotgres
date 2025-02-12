package io.kotgres.orm.exceptions.entity

import io.kotgres.orm.exceptions.base.KotgresException

class KotgresNoMapperException(columnName: String?, typeName: String) :
    KotgresException("Could not find mapper for type $typeName ${if (columnName != null) "(column $columnName)" else ""}. Create a custom mapper for ${typeName.split(".").last()} to fix it (link to docs here)")
