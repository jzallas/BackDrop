package com.jzallas.backdrop.extensions.collections

import java.util.Queue

fun <T> Queue<T>.peekOrNull(): T? = this.takeIf { it.isNotEmpty() }?.peek()

fun <T> Queue<T>.removeOrNull(): T? = this.takeIf { it.isNotEmpty() }?.remove()
