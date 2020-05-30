package com.jzallas.backdrop.repository

import android.net.Uri
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.util.MimeTypes
import com.jzallas.backdrop.exo.TaggedMediaSource
import com.jzallas.backdrop.exo.withTag
import com.jzallas.backdrop.repository.model.MediaSample

class MediaSourceRepository(
  private val youTubeRepository: YouTubeRepository,
  private val factory: MediaSourceFactory
) {

  fun getSource(url: String): TaggedMediaSource<MediaSample> {
    val info = youTubeRepository.getVideoInfo(url)

    // best format
    val format = info.formats
      .filter { MimeTypes.isAudio(it.mimeType) }
      .maxBy { it.audioBitrate ?: 0 }
      ?: throw IllegalStateException("Unable to find a compatible audio stream within format list.")

    val (lowResThumbnail, highResThumbnail) = info.thumbnails
      .sortedBy { it.height * it.width }
      .let { it.first() to it.last() }

    val sample = MediaSample(
      title = info.title,
      sourceUrl = url,
      thumbnailUrl = lowResThumbnail.url,
      previewUrl = highResThumbnail.url
    )

    val uri = Uri.parse(format.url)

    return factory.createMediaSource(uri).withTag(sample)
  }
}
