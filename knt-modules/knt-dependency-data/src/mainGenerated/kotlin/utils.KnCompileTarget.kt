package dev.adamko.kntoolchain.tools.data


val KnCompileTarget.OsFamily.isLinux: Boolean
   get() = this == Linux

val KnCompileTarget.OsFamily.isWindows: Boolean
   get() = this == Mingw

val KnCompileTarget.OsFamily.isAndroid: Boolean
   get() = this == Android

val KnCompileTarget.OsFamily.isApple: Boolean
   get() = this == OsX || this == IOs || this == WatchOs || this == TvOs

val KnCompileTarget.isLinux: Boolean
  get() = os.isLinux

val KnCompileTarget.isWindows: Boolean
  get() = os.isWindows

val KnCompileTarget.isAndroid: Boolean
  get() = os.isAndroid

val KnCompileTarget.isApple: Boolean
  get() = os.isApple
