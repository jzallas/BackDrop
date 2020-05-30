package com.jzallas.backdrop.repository

import com.jzallas.backdrop.webview.WebViewFactory
import com.jzallas.backdrop.repository.model.VideoInfo
import com.jzallas.webview.promise.call
import com.jzallas.webview.html.loadAsset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class YouTubeApi(
  private val factory: WebViewFactory,
  private val asset: String,
  private val origin: String,
  private val json: Json
) {

  // lazily create this on the first request since it needs to be constructed on the main looper
  private val webView by lazy { factory.create() }

  fun getVideoInfo(url: String): VideoInfo {
    return runBlocking {
      val args = json.toJson(String.serializer(), url).toString()

      val result = withContext(Dispatchers.Main) {
        webView.loadAsset(origin, asset)
        webView.call("getVideoInfo", args)
      }

      json.parse(VideoInfo.serializer(), result)
    }
  }
}
