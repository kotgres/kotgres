package io.kotgres.dao.primarykey.general.classes

import io.kotgres.dao.primarykey.general.TEST_PKEY_NAMES_TABLE_NAME
import io.kotgres.orm.annotations.Column
import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table

@Table(name = TEST_PKEY_NAMES_TABLE_NAME)
class PrimaryKeyWithDifferentName(
    @PrimaryKey
    @Generated
    @Column(name = "id")
    val myKotlinId: Int,
    var int: Int,
)
