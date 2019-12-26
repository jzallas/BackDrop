package com.jzallas.backdrop.javascript.webview

import android.webkit.WebView
import com.jzallas.backdrop.javascript.JavascriptEngine
import com.jzallas.backdrop.random.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

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
  // wait until the first request is made to create this
  private val controller by lazy { factory.create(this) }

  override fun call(entryPoint: String, function: String, args: String): String {
    return runBlocking { enqueue(entryPoint, function, args) }
  }

  private suspend fun enqueue(entryPoint: String, function: String, args: String): String =
    withContext(Dispatchers.Main) { controller.call(origin, entryPoint, function, args) }
}
