package com.jzallas.backdrop.repository

import com.google.android.exoplayer2.util.MimeTypes
import com.jzallas.backdrop.repository.model.Format
import com.jzallas.backdrop.repository.model.MediaSample
import com.jzallas.backdrop.repository.model.VideoInfo

class MediaSampleRepository(
  private val youTubeRepository: YouTubeRepository
) {

  fun getSample(url: String): MediaSample {
    val info = youTubeRepository.getVideoInfo(url)

    return info.formats
      .filter { MimeTypes.isAudio(it.mimeType) }
      .sortedByDescending { it.audioBitrate }
      .asSequence()
      .map { createAudioSample(url, info, it) }
      .first()
  }

  private fun createAudioSample(source: String, info: VideoInfo, format: Format): MediaSample {
    val (lowRes, highRes) = info.thumbnails
      .sortedBy { it.height * it.width }
      .let { it.first() to it.last() }

    return MediaSample(
      title = info.title,
      sourceUrl = source,
      streamUrl = format.url,
      thumbnailUrl = lowRes.url,
      previewUrl = highRes.url
    )
  }
}
