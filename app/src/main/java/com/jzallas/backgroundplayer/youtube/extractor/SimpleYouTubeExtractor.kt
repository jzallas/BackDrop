package com.jzallas.backgroundplayer.youtube.extractor

import android.content.Context
import android.util.SparseArray
import at.huber.youtubeExtractor.VideoMeta
import at.huber.youtubeExtractor.YtFile
import com.jzallas.backgroundplayer.youtube.api.YouTubeUrl
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

private typealias OnExtractionCompleteListener = (SparseArray<YtFile>?) -> Unit

class YouTubeExtractor(context: Context) {

  private val context = context.applicationContext

  suspend fun extract(
    url: YouTubeUrl,
    parseDashManifest: Boolean = true,
    includeWebM: Boolean = true
  ) : SparseArray<YtFile>? = suspendCoroutine { continuation ->
      Extractor(context)
        .onExtractionComplete { continuation.resume(it) }
        .extract(url.toString(), parseDashManifest, includeWebM)
    }

  private class Extractor(context: Context) : at.huber.youtubeExtractor.YouTubeExtractor(context) {

    var callback: OnExtractionCompleteListener? = null

    fun onExtractionComplete(callback: OnExtractionCompleteListener) = this.apply {
      this.callback = callback
    }

    override fun onExtractionComplete(ytFiles: SparseArray<YtFile>?, videoMeta: VideoMeta?) {
      callback?.invoke(ytFiles)
    }
  }
}
