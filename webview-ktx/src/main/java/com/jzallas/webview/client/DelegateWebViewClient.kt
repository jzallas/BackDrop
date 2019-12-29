package com.jzallas.webview.client

import android.graphics.Bitmap
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature

open class DelegateWebViewClient : WebViewClientCompat() {

  val onPageStarted = mutableSetOf<(view: WebView, url: String, favicon: Bitmap?) -> Unit>()
  override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
    onPageStarted.forEach { it.invoke(view, url, favicon) }
    super.onPageStarted(view, url, favicon)
  }

  val onPageFinished = mutableSetOf<(view: WebView, url: String) -> Unit>()
  override fun onPageFinished(view: WebView, url: String) {
    onPageFinished.forEach { it.invoke(view, url) }
    super.onPageFinished(view, url)
  }

  val shouldInterceptRequest = mutableSetOf<(view: WebView, request: WebResourceRequest) -> WebResourceResponse?>()
  override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
    return shouldInterceptRequest.asSequence()
      .mapNotNull { it.invoke(view, request) }
      .firstOrNull()
      ?: super.shouldInterceptRequest(view, request)
  }
}

internal val WebView.delegateClient: DelegateWebViewClient?
  get() = when {
    WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_CLIENT) ->
      WebViewCompat.getWebViewClient(this) as? DelegateWebViewClient
    else -> null
  }
