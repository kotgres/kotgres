package io.kotgres.orm.internal

import org.slf4j.LoggerFactory

/**
 * Annotation Processor Logger
 */
internal object ApLogger {

    private val logger = LoggerFactory.getLogger("KOTGRES")

    fun debug(msg: String) {
        logger.debug(msg)
    }

    fun info(msg: String) {
        logger.info(msg)
    }

    fun warn(msg: String) {
        logger.warn(msg)
    }

    fun error(msg: String) {
        logger.error(msg)
    }
}
