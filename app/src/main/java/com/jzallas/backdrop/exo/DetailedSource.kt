package com.jzallas.backdrop.exo

import android.net.Uri
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.jzallas.backdrop.extensions.castOrNull

/**
 * [MediaSource] combined with related [details] metadata.
 */
class DetailedSource<T> internal constructor(
  val details: T,
  source: MediaSource
) : ComposedMediaSource<MediaSource>(source) {

  interface Factory {
    fun <T> createMediaSource(uri: Uri, details: T): DetailedSource<T>
  }

  override fun hashCode() = details.hashCode()

  override fun equals(other: Any?) = other?.castOrNull<DetailedSource<T>>()?.details == details
}

class DetailedSourceFactory(
  private val factory: MediaSourceFactory
) : DetailedSource.Factory {
  override fun <T> createMediaSource(uri: Uri, details: T) = DetailedSource(details, factory.createMediaSource(uri))
}
