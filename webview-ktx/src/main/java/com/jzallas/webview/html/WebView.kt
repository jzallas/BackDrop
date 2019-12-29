package com.jzallas.webview.html

import android.net.Uri
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
 * This will only load distinct requests. If multiple requests are made with the same args,
 * this function will not reload the html into as it should already exist within the [WebView].
 *
 * ```
 * webView.loadHtml("foo", "<p>bar</p>") // loads
 * webView.loadHtml("foo", "<p>bar</p>") // ignored
 * webView.loadHtml("foo", "<p>baz</p>") // loads
 * ```
 *
 * @param origin the url to associate with the html
 * @param html the raw html to load
 */
suspend fun WebView.loadHtml(origin: String, html: String) = suspendCoroutine<Unit> { continuation ->
  loadHtml(origin, html) { continuation.resume(Unit) }
}

private fun WebView.loadHtml(origin: String, html: String, onComplete: () -> Unit) {
  val generated = generateOrigin(origin, html.hashCode())

  if (generated == uri) {
    // this combination of origin + html has already been loaded into the WebView
    onComplete.invoke()
    return
  }

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

  loadDataWithBaseURL(
    generated.toString(),
    html,
    "text/html",
    Charsets.UTF_8.toString(),
    generated.toString()
  )

  // if we couldn't attach the callback, then at least notify that we tried to complete
  client ?: onComplete.invoke()
}

private val WebView.uri: Uri?
  get() = runCatching { Uri.parse(url) }.getOrNull()

private fun generateOrigin(origin: String, hash: Int) =
  Uri.parse(origin)
    .buildUpon()
    .clearQuery()
    .clearPath()
    .appendQueryParameter("hash", hash.toString())
    .build()

private fun Uri.Builder.clearPath() = this.apply { path("/") }
