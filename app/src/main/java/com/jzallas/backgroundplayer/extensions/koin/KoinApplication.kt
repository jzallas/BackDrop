package com.jzallas.backgroundplayer.extensions.koin

import org.koin.core.KoinApplication
import org.koin.core.module.Module

fun KoinApplication.modules(vararg modules: Module) {
  modules(modules.toList())
}
