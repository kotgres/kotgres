package io.kotgres.orm.internal.utils

import com.google.devtools.ksp.symbol.KSFile

// Singleton to make sharing between builders/processors/visitors easier
// DaoBuilder adds to these lists and DaoManagerBuilder uses the info
internal object DaoInfo {
    val createPkeyDaos = mutableListOf<Pair<String, String>>()
    val createPkeyDaosOriginatingKSFiles = mutableListOf<KSFile>()
    val createNoPkeyDaos = mutableListOf<Pair<String, String>>()
    val createNoPkeyDaosOriginatingKSFiles = mutableListOf<KSFile>()
}