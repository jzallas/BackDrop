package com.jzallas.backdrop.webview

import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.jzallas.backdrop.BuildConfig
import com.jzallas.webview.promise.javaScriptEnabled

class WebViewFactory(
  context: Context,
  private val agent: String,
  private val client: WebViewClient
) {
  private val appContext = context.applicationContext

  fun create() = WebView(appContext).apply {
    WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)

    webViewClient = client

    javaScriptEnabled = true

    settings.apply {
      userAgentString = agent
    }
    setWillNotDraw(true)
  }
}
