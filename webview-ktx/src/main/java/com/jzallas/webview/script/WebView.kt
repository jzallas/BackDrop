package com.jzallas.webview.script

import android.graphics.Bitmap
import android.webkit.WebView
import androidx.webkit.WebViewClientCompat
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun WebView.loadHtml(origin: String, html: String) = suspendCoroutine<Unit> { continuation ->
  loadHtml(origin, html) { continuation.resume(Unit) }
}

private fun WebView.loadHtml(origin: String, html: String, onComplete: () -> Unit) {
  var client: DelegateWebViewClient? = null
  if (WebViewFeature.isFeatureSupported(WebViewFeature.GET_WEB_VIEW_CLIENT)) {
    client = WebViewCompat.getWebViewClient(this) as? DelegateWebViewClient
  }

  // attach the callback first before loading
  client?.run {
    onPageFinished += object : (WebView, String) -> Unit {
      override fun invoke(view: WebView, url: String) {
        onPageFinished -= this
        onComplete.invoke()
      }
    }
  }

  loadDataWithBaseURL(origin, html, "text/html", Charsets.UTF_8.toString(), origin)

  // if we couldn't attach the callback, then at least notify that we tried to complete
  client ?: onComplete.invoke()
}

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
}
