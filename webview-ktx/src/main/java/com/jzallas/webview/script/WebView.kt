package com.jzallas.webview.script

import android.webkit.WebView
import com.jzallas.webview.client.delegateClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

suspend fun WebView.loadAsset(origin: String, asset: String) {
  val html = withContext(Dispatchers.IO) {
    context.assets
      .open(asset)
      .bufferedReader()
      .use { it.readText() }
  }
  loadHtml(origin, html)
}

suspend fun WebView.loadHtml(origin: String, html: String) = suspendCoroutine<Unit> { continuation ->
  loadHtml(origin, html) { continuation.resume(Unit) }
}

private fun WebView.loadHtml(origin: String, html: String, onComplete: () -> Unit) {
  val client = delegateClient

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
