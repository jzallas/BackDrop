package com.jzallas.backdrop.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.jzallas.backdrop.MainActivity
import com.jzallas.backdrop.extensions.log.logInfo
import com.jzallas.backdrop.repository.MediaSourceRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject
import org.koin.core.parameter.parametersOf
import kotlin.coroutines.CoroutineContext
import android.os.Binder
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.LifecycleService
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.jzallas.backdrop.exo.DetailedSource
import com.jzallas.backdrop.exo.PlayList
import com.jzallas.backdrop.extensions.distinct
import com.jzallas.backdrop.extensions.glide.into
import com.jzallas.backdrop.repository.model.MediaSample

private typealias NotificationManager = PlayerNotificationManager
private typealias MediaDescriptionAdapter = PlayerNotificationManager.MediaDescriptionAdapter
private typealias BitmapCallback = PlayerNotificationManager.BitmapCallback
private typealias NotificationListener = PlayerNotificationManager.NotificationListener
private typealias EventListener = Player.EventListener

class MediaService : LifecycleService(), MediaDescriptionAdapter, NotificationListener, EventListener, CoroutineScope {
  companion object {
    const val SESSION_TAG = "com.jzallas.backdrop.service.MediaService"
    fun createIntent(context: Context, original: Intent): Intent =
      Intent(original)
        .setClass(context, MediaService::class.java)
  }

  private lateinit var job: Job
  override val coroutineContext: CoroutineContext
    get() = job + Dispatchers.Main

  private lateinit var connector: MediaSessionConnector

  private lateinit var mediaSession: MediaSessionCompat

  val player: ExoPlayer by inject()

  private val playList by lazy { PlayList<DetailedSource<MediaSample>>() }

  private val notificationManager: NotificationManager by inject { parametersOf(this, this) }

  private val sourceRepository: MediaSourceRepository by inject()

  var onSamplePrepared : ((MediaSample) -> Unit)? = null
    set(value) {
      field = value
      // notify immediately if a sample is already playing
      nowPlaying?.let { value?.invoke(it) }
    }

  private var currentIndex by distinct<Int?>(null) { _, new ->
    new ?: return@distinct
    // every time the currentIndex changes, notify listeners about the new content
    playList[new].details.also { onSamplePrepared?.invoke(it) }
  }

  private val nowPlaying: MediaSample?
    get() = currentIndex?.let { playList[it] }?.details

  override fun onBind(intent: Intent): IBinder {
    super.onBind(intent)
    return LocalBinder()
  }

  override fun onCreate() {
    job = Job()
    notificationManager.setPlayer(player)
    player.addListener(this)

    mediaSession = MediaSessionCompat(this, SESSION_TAG)
    mediaSession.isActive = true

    lifecycle.addObserver(BecomingNoisyReceiver(this, mediaSession))

    notificationManager.setMediaSessionToken(mediaSession.sessionToken)
    connector = MediaSessionConnector(mediaSession)

    connector.setQueueNavigator(object : TimelineQueueNavigator(mediaSession) {
      override fun getMediaDescription(player: Player?, windowIndex: Int): MediaDescriptionCompat {
        val sample = playList[windowIndex].details

        // TODO - consider pre-loading bitmap
        val bitmap: Bitmap? = null

        val extras = bundleOf(
          MediaMetadataCompat.METADATA_KEY_ALBUM to bitmap,
          MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON to bitmap
        )

        return MediaDescriptionCompat.Builder()
          .setMediaId(sample.id.toString())
          .setIconBitmap(bitmap)
          .setTitle(sample.title)
          .setDescription(sample.sourceUrl)
          .setExtras(extras)
          .build()
      }

    })

    connector.setPlayer(player)

    player.run {
      prepare(playList)
    }

    super.onCreate()
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    intent.extras
      ?.getString(Intent.EXTRA_TEXT)
      ?.let { prepareAudio(it) }
      ?: logInfo("Could not extract url from onStartCommand")
    return super.onStartCommand(intent, flags, startId)
  }

  private fun prepareAudio(url: String) {
    launch {
      val source = withContext(Dispatchers.IO) { sourceRepository.getSource(url) }
      playList.add(source)
      player.play(playList.size - 1)
    }
  }

  private fun Player.play(index: Int) {
    when (currentIndex) {
      // set the current index the first time around
      null -> currentIndex = index
      // same index, just replay from beginning
      index -> seekToDefaultPosition()
      // playlist size has changed - navigate to the requested index
      else -> seekToDefaultPosition(index)
    }
    playWhenReady = true
  }

  override fun onDestroy() {
    mediaSession.release()
    player.release()
    notificationManager.setPlayer(null)
    connector.setPlayer(null)
    job.cancel()
    super.onDestroy()
  }

  override fun createCurrentContentIntent(player: Player): PendingIntent =
    Intent(this, MainActivity::class.java)
      .let { PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_UPDATE_CURRENT) }

  override fun getCurrentContentTitle(player: Player?) =
    nowPlaying?.title

  override fun getCurrentContentText(player: Player) =
    nowPlaying?.sourceUrl

  override fun getCurrentLargeIcon(player: Player, callback: BitmapCallback): Bitmap? {
    val url = nowPlaying?.thumbnailUrl ?: return null

    return Glide.with(this)
      .asBitmap()
      .load(url)
      .into(callback)
  }


  override fun onNotificationPosted(id: Int, notification: Notification?, ongoing: Boolean) {
    if (ongoing) startForeground(id, notification)
  }

  override fun onNotificationCancelled(id: Int, dismissedByUser: Boolean) {
    if (dismissedByUser) stopSelf()
  }

  override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
    val isPlaying = playWhenReady && playbackState == Player.STATE_READY
    val isStopped = playWhenReady && playbackState == Player.STATE_ENDED
    val isPaused = !playWhenReady
    when {
      isPlaying -> logInfo("Playback has begun/resumed.")
      isStopped -> stopForeground(false).also { logInfo("Playback has finished.") }
      isPaused -> stopForeground(false).also { logInfo("Playback has been paused.") }
    }
  }

  override fun onPositionDiscontinuity(reason: Int) {
    super.onPositionDiscontinuity(reason)
    // update the current index as the window may have changed
    currentIndex = player.currentWindowIndex
  }

  inner class LocalBinder : Binder() {
    val service: MediaService
      get() = this@MediaService
  }
}


