package io.kotgres.orm.internal.utils

internal fun String.toSnakeCase(): String {
    var isPreviousUppercase = false
    return this.fold(StringBuilder()) { acc, c ->
        acc.let {
            val lowerC = c.lowercase()
            acc.append(if (acc.isNotEmpty() && c.isUpperCase() && !isPreviousUppercase) "_$lowerC" else lowerC)
            isPreviousUppercase = c.isUpperCase()
            acc
        }
    }.toString()
}

internal fun List<String>.joinWithDifferentLastSeparator(separator: String, lastSeparator: String): String {
    return when (this.size) {
        0 -> ""
        1 -> this[0]
        else -> this.dropLast(1).joinToString(separator) + lastSeparator + this.last()
    }
}