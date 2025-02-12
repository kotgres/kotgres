package io.kotgres.orm

import io.kotgres.orm.internal.utils.Debug

object Kotgres {
    fun setDebug(enabled: Boolean) {
        Debug.ENABLED = enabled
    }
}
