package com.jzallas.backdrop.youtube.repository

import android.util.SparseArray
import at.huber.youtubeExtractor.YtFile
import com.jzallas.backdrop.youtube.extractor.YouTubeExtractor
import com.jzallas.backdrop.youtube.api.YouTubeUrlParser
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
