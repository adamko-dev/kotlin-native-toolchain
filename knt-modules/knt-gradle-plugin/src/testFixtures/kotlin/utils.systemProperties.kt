package dev.adamko.kntoolchain.test_utils

import kotlin.properties.ReadOnlyProperty

fun systemProperty(): ReadOnlyProperty<Any?, String> =
  systemProperty { it }

fun <T> systemProperty(
  convert: (String) -> T
): ReadOnlyProperty<Any?, T> =
  ReadOnlyProperty { _, property ->
    val value = requireNotNull(System.getProperty(property.name)) {
      "system property ${property.name} is unavailable"
    }
    convert(value)
  }

val kntGradlePluginProjectVersion: String by systemProperty()
