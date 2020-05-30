package com.jzallas.backdrop.exo

import com.google.android.exoplayer2.source.MediaSource
import com.jzallas.backdrop.extensions.castOrNull

/**
 * [MediaSource] combined with related tagged metadata.
 */
class TaggedMediaSource<T> constructor(
  tag: T,
  source: MediaSource
) : ComposedMediaSource<MediaSource>(source) {
  private var _tag = tag

  fun setTag(tag: T) {
    _tag = tag
  }

  override fun getTag(): T = _tag
}

inline fun <reified T> MediaSource.withTag(tag: T): TaggedMediaSource<T> {
  return this.castOrNull<TaggedMediaSource<T>>()
    ?.apply { setTag(tag) }
    ?: TaggedMediaSource(tag, this)
}
