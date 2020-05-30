package com.jzallas.backdrop.exo

import android.os.Handler
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.MediaSource
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A mutable collection of [MediaSources][MediaSource] organized in playback order.
 *
 * Behaves similar to a [HashSet] in that no two [source tags][MediaSource.getTag] are allowed to share the same
 * unique hash. This list can also be used as a [MediaSource] itself with [ExoPlayer.prepare].
 *
 * @property handler used for async modifications to the underlying [ConcatenatingMediaSource]
 * @param concatenatingMediaSource underlying [MediaSource] for handling source synchronization
 */
class PlayList<T>(
  private val handler: Handler = Handler(),
  concatenatingMediaSource: ConcatenatingMediaSource = ConcatenatingMediaSource()
) : ComposedMediaSource<ConcatenatingMediaSource>(concatenatingMediaSource),
  Collection<MediaSource> {

  override val size: Int
    get() = source.size

  operator fun get(index: Int) = source.getMediaSource(index)

  fun getTag(index: Int) = source.getMediaSource(index).tag as T

  override fun contains(element: MediaSource) =
    find { it.tag == element.tag } != null

  override fun containsAll(elements: Collection<MediaSource>) =
    elements.all { contains(it) }

  override fun isEmpty() =
    size == 0

  override fun iterator() =
    iterator {
      for (i in 0 until size) {
        yield(this@PlayList[i])
      }
    }

  suspend fun add(element: TaggedMediaSource<T>): Boolean = suspendCoroutine { continuation ->
    when {
      contains(element) -> continuation.resume(false)
      else -> source.addMediaSource(element, handler) { continuation.resume(true) }
    }
  }


  suspend fun addAll(elements: Collection<TaggedMediaSource<T>>): Boolean = suspendCoroutine { continuation ->
    val newElements = elements.filter { !this.contains(it) }
    when {
      newElements.isEmpty() -> continuation.resume(false)
      else -> source.addMediaSources(newElements, handler) { continuation.resume(true) }
    }
  }

  suspend fun clear(): Unit = suspendCoroutine { continuation ->
    source.clear(handler) { continuation.resume(Unit) }
  }

  suspend fun remove(element: TaggedMediaSource<T>): Boolean = suspendCoroutine { continuation ->
    when (val index = indexOf(element)) {
      -1 -> continuation.resume(false)
      else -> source.removeMediaSource(index, handler) { continuation.resume(true) }
    }
  }
}


