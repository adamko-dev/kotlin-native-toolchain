package dev.adamko.kntoolchain.internal

import org.gradle.api.logging.LogLevel
import org.gradle.api.logging.Logger


internal fun Logger.debug(msg: () -> String): Unit = log(LogLevel.DEBUG, msg)

internal fun Logger.info(msg: () -> String): Unit = log(LogLevel.INFO, msg)

internal fun Logger.lifecycle(msg: () -> String): Unit = log(LogLevel.LIFECYCLE, msg)

internal fun Logger.warn(msg: () -> String): Unit = log(LogLevel.WARN, msg)

internal fun Logger.quiet(msg: () -> String): Unit = log(LogLevel.QUIET, msg)

internal fun Logger.error(msg: () -> String): Unit = log(LogLevel.ERROR, msg)

internal fun Logger.log(level: LogLevel, msg: () -> String) {
  if (isEnabled(level)) {
    log(level, msg())
  }
}
