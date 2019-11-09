package com.jzallas.backgroundplayer.youtube.repository

import android.util.SparseArray
import at.huber.youtubeExtractor.YtFile
import com.jzallas.backgroundplayer.youtube.extractor.YouTubeExtractor
import com.jzallas.backgroundplayer.youtube.api.YouTubeUrlParser
import kotlinx.coroutines.runBlocking

class YtFileRepository(
  private val extractor: YouTubeExtractor,
  private val urlParser: YouTubeUrlParser
) {

  fun getYtFiles(url: String): SparseArray<YtFile> = runBlocking {
    extractor.extract(
      url = urlParser.parse(url),
      parseDashManifest = true,
      includeWebM = true
    )
  } ?: throw IllegalStateException("Unable to fetch ytFiles.")
}
