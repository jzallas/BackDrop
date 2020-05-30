package com.jzallas.backdrop.exo

import android.net.Uri
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.drm.DrmSessionManager
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.util.Util

/**
 * [MediaSourceFactory] that attempts to infer the type of a uri prior to generating a [MediaSource].
 *
 * Delegates media source creation to the first [mediaSourceFactories] that supports it.
 */
class DynamicTypeMediaSourceFactory(
  private val mediaSourceFactories: List<MediaSourceFactory>
) : MediaSourceFactory {

  private val _supportedTypes by lazy {
    mediaSourceFactories.flatMap { it.supportedTypes.toList() }
      .toSet()
      .toIntArray()
  }

  override fun getSupportedTypes(): IntArray = _supportedTypes

  override fun setDrmSessionManager(drmSessionManager: DrmSessionManager<*>?) = this.apply {
    mediaSourceFactories.forEach { it.setDrmSessionManager(drmSessionManager) }
  }

  override fun createMediaSource(uri: Uri): MediaSource {
    return createMediaSource(uri, null)
  }

  /**
   * Creates a [MediaSource] with an optional [contentType] override.
   *
   * @param contentType see [C.ContentType]
   */
  fun createMediaSource(uri: Uri, contentType: Int?): MediaSource {
    val inferredType = contentType ?: Util.inferContentType(uri)

    val factory = mediaSourceFactories.firstOrNull { it.supportedTypes.contains(inferredType) }
      ?: throw IllegalArgumentException("Cannot find appropriate MediaSourceFactory. Unable to infer type of uri: $uri")

    return factory.createMediaSource(uri)
  }
}
