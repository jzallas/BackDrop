package com.jzallas.backdrop.javascript.webview

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.jzallas.backdrop.extensions.log.logInfo
import com.jzallas.backdrop.extensions.log.logWarn
import com.jzallas.backdrop.javascript.JavascriptEngine
import com.jzallas.backdrop.random.IdGenerator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * Implementation of [JavascriptEngine] that uses a [WebView] to execute javascript.
 *
 * @property origin the base url that the [WebView] will make same-origin requests against. Required for CORS.
 * @property idGenerator generates unique identifiers for each call to minimize chances of collision on responses
 * @property factory used to create a [WebViewController] lazily. This means the first request will probably
 * take a little longer since it needs to create the controller.
 */
class WebViewEngine(
  private val origin: String,
  private val idGenerator: IdGenerator,
  private val factory: WebViewController.Factory
): JavascriptEngine {
  private val scope = CoroutineScope(Job() + Dispatchers.Main)

  private val responseMap = mutableMapOf<String, Continuation<String>>()

  // wait until the first request is made to create this
  private val controller by lazy { factory.create(this) }

  override fun call(entryPoint: String, function: String, args: String): String {
    return runBlocking { enqueue(entryPoint, function, args) }
  }

  @JavascriptInterface
  fun onSuccess(id: String, payload: String) {
    responseMap.remove(id)
      ?.resume(payload)
      ?: logWarn("Could not invoke onSuccess for request: $id")
  }

  @JavascriptInterface
  fun onFailure(id: String, payload: String) {
    responseMap.remove(id)
      ?.resumeWithException(JavascriptException(payload))
      ?: logWarn("Could not invoke onFailure for request: $id")
  }

  private suspend fun enqueue(entryPoint: String, function: String, args: String): String =
    suspendCoroutine { continuation ->
      // webview functions must be called on a thread with a looper
      scope.launch {
        val id = idGenerator.createRandomId()
        responseMap[id] = continuation
        val javascript = """$function('$id', $args);"""
        controller.enqueue(origin, entryPoint, javascript) { logInfo("Javascript evaluated: $javascript") }
      }
    }

  class JavascriptException(payload: String) : Exception("Javascript execution failed: $payload")
}
