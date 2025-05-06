package io.kotgres.orm.internal.utils

import com.google.devtools.ksp.symbol.KSFile

data class DaoInfo(
    val packageName: String,
    val daoClassName: String,
    val ksFile: KSFile?,
    val isPrimaryKey: Boolean,
)

// Singleton to make sharing between DaoBuilder and DaoManagerBuilder easier
// Contains all the DaoInfo from Daos generated during this source set execution
internal object GeneratedDaoInfoHolder {
    val allDaos = mutableListOf<DaoInfo>()
}
