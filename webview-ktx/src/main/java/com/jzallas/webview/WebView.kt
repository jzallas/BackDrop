package com.jzallas.webview

import android.util.Log
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.jzallas.webview.ktx.view.getOrSetTag
import java.util.Collections
import java.util.UUID
import kotlin.coroutines.suspendCoroutine

/**
 * Calls a JavaScript function. Supports both blocking and async functions.
 *
 * All calls into JavaScript are considered asynchronous from the kotlin side.
 *
 * Example:
 *
 * Assuming one of the following JavaScript functions is available on the WebView:
 * ```javascript
 * // blocking
 * const foo = (bar) => bar
 *
 * // or async
 * const foo = async (bar) => bar
 * ```
 *
 * The above function can be accessed from Android by calling:
 * ```kotlin
 * coroutineScope.launch {
 *  val result = webView.call("foo", "bar")
 *  // result will be "bar"
 * }
 * ```
 *
 * @param functionName the name of the function to call
 * @param args the arguments to provide to the function in json format
 * @return the return value of the function in json format
 * @throws PromiseException if the JavaScript promise failed
 */
@Throws(PromiseException::class)
suspend fun WebView.call(functionName: String, args: String): String =
  suspendCoroutine { continuation ->
    val map = javascriptPromiseMap

    // generate a unique identifier so that we can map the promise back to the correct callback
    val id = UUID.randomUUID().toString()

    val javascript = """
      (() => {
        let value = $functionName(JSON.parse('$args'))
        
        Promise.resolve(value)
          .then(result => ${map.name}.onSuccess('$id', JSON.stringify(result)))
          .catch(error => ${map.name}.onFailure('$id', JSON.stringify(error)))
      })()
    """.trimIndent()

    map.add(id) { continuation.resumeWith(it) }

    evaluateJavascript(javascript) { log(javascript) }
  }

/**
 * Enables JavaScript via [android.webkit.WebSettings] and configures this [WebView]
 * to be compatible with JavaScript promises.
 */
var WebView.javaScriptEnabled
  get() = settings.javaScriptEnabled
  set(value) {
    if (settings.javaScriptEnabled == value) return

    settings.javaScriptEnabled = value
    javascriptPromiseMap.let {
      when (value) {
        true -> addJavascriptInterface(it, it.name)
        false -> removeJavascriptInterface(it.name)
      }
    }
  }

private val WebView.javascriptPromiseMap: JavascriptPromiseMap
  get() = getOrSetTag(R.id.webViewPromiseMap) { JavascriptPromiseMap() } as JavascriptPromiseMap

private typealias PromiseCallback = (Result<String>) -> Unit

private class JavascriptPromiseMap {

  private val map = Collections.synchronizedMap(mutableMapOf<String, PromiseCallback>())

  internal val name get() = "JavascriptPromiseMap"

  internal fun add(id: String, action: PromiseCallback) {
    map[id] = action
  }

  @JavascriptInterface
  fun onSuccess(id: String, payload: String) {
    map.remove(id)
      ?.invoke(Result.success(payload))
      ?: log("Could not find javascript callback.")
  }

  @JavascriptInterface
  fun onFailure(id: String, payload: String) {
    map.remove(id)
      ?.invoke(Result.failure(PromiseException(payload)))
      ?: log("Could not find javascript callback.")
  }
}

/**
 * Encapsulates a JavaScript promise failure
 */
class PromiseException(message: String) : Exception(message)

private fun log(message: String) {
  Log.d("JavascriptPromise", message)
}
