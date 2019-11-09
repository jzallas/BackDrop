package com.jzallas.backgroundplayer.extensions.log

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
