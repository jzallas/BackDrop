package com.jzallas.backdrop.exo

import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.upstream.TransferListener

/**
 * Base class for composing [MediaSources][MediaSource].
 *
 * Kotlin delegation isn't sufficient for wrapping a [MediaSource].
 * see: [https://github.com/google/ExoPlayer/issues/3663]
 */
abstract class ComposedMediaSource<T : MediaSource>(
  internal val source: T
) : MediaSource by source {
  override fun prepareSource(caller: MediaSource.MediaSourceCaller?, mediaTransferListener: TransferListener?) {
    val topLevelListener = MediaSource.MediaSourceCaller { _, timeline ->
      caller?.onSourceInfoRefreshed(this@ComposedMediaSource, timeline)
    }
    source.prepareSource(topLevelListener, mediaTransferListener)
  }
}
