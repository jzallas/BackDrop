package com.jzallas.webview.html

import android.webkit.WebView
import com.jzallas.webview.client.delegateClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Loads an asset into this [WebView] as html.
 * Suspends until the page is done loading.
 *
 * @param origin the url to associate with the html
 * @param asset the location of the html in `/assets`.
 *  For example: `/assets/www/index.html` should just be `www/index.html`
 *
 * @see loadHtml
 */
suspend fun WebView.loadAsset(origin: String, asset: String) {
  val html = withContext(Dispatchers.IO) {
    context.assets
      .open(asset)
      .bufferedReader()
      .use { it.readText() }
  }
  loadHtml(origin, html)
}

/**
 * Loads html into this [WebView].
 * Suspends until the page is done loading.
 *
 * @param origin the url to associate with the html
 * @param html the raw html to load
 */
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
