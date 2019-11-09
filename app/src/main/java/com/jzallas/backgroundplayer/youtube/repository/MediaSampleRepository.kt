package com.jzallas.backgroundplayer.youtube.repository

import at.huber.youtubeExtractor.Format
import at.huber.youtubeExtractor.YtFile
import com.jzallas.backgroundplayer.extensions.collections.asIterable
import com.jzallas.backgroundplayer.youtube.model.MediaSample
import com.jzallas.backgroundplayer.youtube.model.VideoDetails

class MediaSampleRepository(
  private val videoDetailRepository: VideoDetailRepository,
  private val ytFileRepository: YtFileRepository
) {

  fun getSample(url: String): MediaSample {
    val details = videoDetailRepository.getDetails(url)
    val ytFiles = ytFileRepository.getYtFiles(url)

    return ytFiles.asIterable()
      .filter { it.format.hasAudio && !it.format.hasVideo } // audio files only
      .sortedByDescending { it.format.audioBitrate } // highest bitrate first
      .asSequence()
      .map { createAudioSample(url, it, details) }
      .first()
  }

  private fun createAudioSample(source: String, ytFile: YtFile, details: VideoDetails): MediaSample {
    val (lowRes, highRes) = details.thumbnails
      .sortedBy { it.height * it.width }
      .let { it.first() to it.last() }

    return MediaSample(
      title = details.title,
      sourceUrl = source,
      streamUrl = ytFile.url,
      thumbnailUrl = lowRes.url,
      previewUrl = highRes.url
    )
  }


  private val Format.hasAudio
    get() = audioBitrate > 0

  private val Format.hasVideo
    get() = height > 0
}
