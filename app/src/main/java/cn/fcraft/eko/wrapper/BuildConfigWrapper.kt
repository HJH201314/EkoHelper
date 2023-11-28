@file:Suppress("unused")
package cn.fcraft.eko.wrapper

import cn.fcraft.eko.BuildConfig

/**
 * 对 [BuildConfig] 的包装
 */
object BuildConfigWrapper {
    const val APPLICATION_ID = BuildConfig.APPLICATION_ID
    const val VERSION_NAME = BuildConfig.VERSION_NAME
    const val VERSION_CODE = BuildConfig.VERSION_CODE
    val isDebug = BuildConfig.DEBUG
}