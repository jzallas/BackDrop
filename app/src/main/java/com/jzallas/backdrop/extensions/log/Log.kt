package com.jzallas.backdrop.extensions.log

import android.util.Log

inline fun <reified T : Any> T.logError(message: String, throwable: Throwable? = null) {
  val tag = T::class.java.simpleName
  when (throwable) {
    null -> Log.e(tag, message)
    else -> Log.e(tag, message, throwable)
  }
}

inline fun <reified T : Any> T.logInfo(message: String, throwable: Throwable? = null) {
  val tag = T::class.java.simpleName
  when (throwable) {
    null -> Log.i(tag, message)
    else -> Log.i(tag, message, throwable)
  }
}

inline fun <reified T : Any> T.logWarn(message: String, throwable: Throwable? = null) {
  val tag = T::class.java.simpleName
  when (throwable) {
    null -> Log.w(tag, message)
    else -> Log.w(tag, message, throwable)
  }
}
