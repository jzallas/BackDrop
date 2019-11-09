package com.jzallas.backgroundplayer.extensions

inline fun <reified T> Any.castOrNull(): T? = this as? T

inline fun <reified T> Any.cast(): T = this as T
