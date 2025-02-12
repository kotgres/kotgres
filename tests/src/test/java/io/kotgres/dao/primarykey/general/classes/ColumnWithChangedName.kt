package io.kotgres.dao.primarykey.general.classes

import io.kotgres.dao.primarykey.general.TEST_COLUMN_NAMES_TABLE_NAME
import io.kotgres.orm.annotations.Column
import io.kotgres.orm.annotations.Generated
import io.kotgres.orm.annotations.PrimaryKey
import io.kotgres.orm.annotations.Table

@Table(name = TEST_COLUMN_NAMES_TABLE_NAME)
class ColumnWithChangedName(
    @PrimaryKey
    @Generated
    val id: Int,
    @Column(name = "correct_name")
    var incorrectName: Int,
)
