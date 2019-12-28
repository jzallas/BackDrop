package com.jzallas.backdrop.javascript.webview

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.MainThread
import androidx.webkit.WebViewClientCompat
import com.jzallas.backdrop.BuildConfig
import com.jzallas.backdrop.extensions.collections.peekOrNull
import com.jzallas.backdrop.extensions.collections.removeOrNull
import com.jzallas.webview.promise.call
import com.jzallas.webview.promise.javaScriptEnabled
import java.util.LinkedList
import java.util.Queue
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Directs javascript evaluation within the [WebView].
 *
 * This controller ensures that requests honor the load cycle of the [WebView].
 * It also attempts to prevent requests from colliding by queueing them up and executing them sequentially.
 *
 * As the underlying [webView] is ultimately a [View], requests made to this class must be made against the main thread.
 */
class WebViewController(
  private val webView: WebView,
  private val injector: ScriptInjector
) {

  private val queue: Queue<Request> = LinkedList()

  init {
    webView.webViewClient = LoadClient()
  }

  private fun load() {
    val request = queue.peekOrNull() ?: return

    webView.loadDataWithBaseURL(
      request.origin,
      injector.inject(request.entryPoint),
      "text/html",
      Charsets.UTF_8.toString(),
      "about:blank"
    )
  }

  private fun consume() {
    queue.removeOrNull()
      ?.onLoadComplete
      ?.invoke()
    load()
  }

  /**
   * Add a javascript request into this controller's request queue.
   *
   * @param origin the base url that the webview will make requests against. Required for CORS support.
   * @param entryPoint the location of the javascript asset that we want to execute
   * @param javascript the full javascript command to execute against the entryPoint.
   * This is the same as what you would run in a javascript console after the [entryPoint] has loaded.
   * @param onEvaluated callback used to notify the caller when this function has finished parsing and evaluating.
   * This does not mean that the [javascript] command is done executing (as it could be async).
   */
  @MainThread
  fun enqueue(origin: String, entryPoint: String, javascript: String, onEvaluated: (String) -> Unit) {
    val request = Request(origin, entryPoint) {
      webView.evaluateJavascript(javascript) { onEvaluated.invoke(it) }
    }
    queue.add(request)
    // if this is the only request, load it immediately
    if (queue.size == 1) load()
  }

  @MainThread
  suspend fun call(origin: String, entryPoint: String, functionName: String, args: String): String {
    load(origin, entryPoint)
    return webView.call(functionName, args)
  }

  private suspend fun load(origin: String, entryPoint: String) = suspendCoroutine<Unit> { continuation ->
    val request = Request(origin, entryPoint) { continuation.resume(Unit) }
    queue.add(request)
    // if this is the only request, load it immediately
    if (queue.size == 1) load()
  }

  private class Request(val origin: String, val entryPoint: String, val onLoadComplete: () -> Unit)

  private inner class LoadClient : WebViewClientCompat() {
    @MainThread
    override fun onPageFinished(view: WebView?, url: String?) {
      super.onPageFinished(view, url)
      consume()
    }

    override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
      return injector.intercept(request) ?: super.shouldInterceptRequest(view, request)
    }
  }

  class Factory(
    context: Context,
    private val agent: String,
    private val name: String,
    private val injector: ScriptInjector
  ) {
    private val appContext = context.applicationContext

    @SuppressLint("JavascriptInterface")
    fun create(jsInterface: Any) = WebView(appContext).apply {
      WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

      javaScriptEnabled = true

      settings.apply {
        userAgentString = agent
      }
      setWillNotDraw(true)

      addJavascriptInterface(jsInterface, name)
    }.let { WebViewController(it, injector) }
  }
}
