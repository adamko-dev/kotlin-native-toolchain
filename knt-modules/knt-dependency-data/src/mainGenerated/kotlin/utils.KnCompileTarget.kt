package dev.adamko.kntoolchain.tools.data


/**
 * `true` if this OS family belongs to the Linux group.
 *
 * Linux group:
 * - [KnCompileTarget.OsFamily.Linux]
 */
val KnCompileTarget.OsFamily.isLinux: Boolean
   get() = this == Linux

/**
 * `true` if this OS family belongs to the Windows group.
 *
 * Windows group:
 * - [KnCompileTarget.OsFamily.Mingw]
 */
val KnCompileTarget.OsFamily.isWindows: Boolean
   get() = this == Mingw

/**
 * `true` if this OS family belongs to the Android group.
 *
 * Android group:
 * - [KnCompileTarget.OsFamily.Android]
 */
val KnCompileTarget.OsFamily.isAndroid: Boolean
   get() = this == Android

/**
 * `true` if this OS family belongs to the Apple group.
 *
 * Apple group:
 * - [KnCompileTarget.OsFamily.OsX]
 * - [KnCompileTarget.OsFamily.IOs]
 * - [KnCompileTarget.OsFamily.WatchOs]
 * - [KnCompileTarget.OsFamily.TvOs]
 */
val KnCompileTarget.OsFamily.isApple: Boolean
   get() = this == OsX || this == IOs || this == WatchOs || this == TvOs

/**
 * `true` if this target's [KnCompileTarget.os] belongs to the Linux OS group.
 *
 * @see KnCompileTarget.OsFamily.isLinux
 */
val KnCompileTarget.isLinux: Boolean
  get() = os.isLinux

/**
 * `true` if this target's [KnCompileTarget.os] belongs to the Windows OS group.
 *
 * @see KnCompileTarget.OsFamily.isWindows
 */
val KnCompileTarget.isWindows: Boolean
  get() = os.isWindows

/**
 * `true` if this target's [KnCompileTarget.os] belongs to the Android OS group.
 *
 * @see KnCompileTarget.OsFamily.isAndroid
 */
val KnCompileTarget.isAndroid: Boolean
  get() = os.isAndroid

/**
 * `true` if this target's [KnCompileTarget.os] belongs to the Apple OS group.
 *
 * @see KnCompileTarget.OsFamily.isApple
 */
val KnCompileTarget.isApple: Boolean
  get() = os.isApple
