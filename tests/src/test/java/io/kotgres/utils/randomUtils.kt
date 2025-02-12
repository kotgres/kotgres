package io.kotgres.utils


private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
fun randomString(size: Int = 16) = List(size) { charPool.random() }.joinToString("")

fun randomNumber(max: Int = 100) = (0..max).random()
