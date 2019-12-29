package com.jzallas.backdrop.javascript.webview

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.annotation.MainThread
import com.jzallas.backdrop.BuildConfig
import com.jzallas.webview.promise.call
import com.jzallas.webview.promise.javaScriptEnabled
import com.jzallas.webview.script.DelegateWebViewClient
import com.jzallas.webview.script.loadHtml

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

  init {
    webView.webViewClient = LoadClient()
  }

  @MainThread
  suspend fun call(origin: String, entryPoint: String, functionName: String, args: String): String {
    webView.loadHtml(origin, injector.inject(entryPoint))
    return webView.call(functionName, args)
  }

  private inner class LoadClient : DelegateWebViewClient() {
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
