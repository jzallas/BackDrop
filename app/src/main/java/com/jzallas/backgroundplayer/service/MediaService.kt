package com.jzallas.backgroundplayer.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.IBinder
import com.bumptech.glide.Glide
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.jzallas.backgroundplayer.MainActivity
import com.jzallas.backgroundplayer.di.MediaSourceFactory
import com.jzallas.backgroundplayer.extensions.glide.into
import com.jzallas.backgroundplayer.extensions.log.logInfo
import com.jzallas.backgroundplayer.youtube.model.MediaSample
import com.jzallas.backgroundplayer.youtube.repository.MediaSampleRepository
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
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import kotlin.properties.Delegates.observable

private typealias NotificationManager = PlayerNotificationManager
private typealias MediaDescriptionAdapter = PlayerNotificationManager.MediaDescriptionAdapter
private typealias BitmapCallback = PlayerNotificationManager.BitmapCallback
private typealias NotificationListener = PlayerNotificationManager.NotificationListener
private typealias EventListener = Player.EventListener

class MediaService : LifecycleService(), MediaDescriptionAdapter, NotificationListener, EventListener, CoroutineScope {
  companion object {
    const val SESSION_TAG = "com.jzallas.backgroundplayer.service.MediaService"
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

  private val mediaSourceFactory: MediaSourceFactory by inject()

  private val notificationManager: NotificationManager by inject { parametersOf(this, this) }

  private val sampleRepository: MediaSampleRepository by inject()

  var onSamplePrepared by observable<((MediaSample) -> Unit)?>(null) { _, _, new ->
    val existingSample = nowPlaying ?: return@observable
    new?.invoke(existingSample)
  }

  private var nowPlaying: MediaSample? = null

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
        val nowPlaying = nowPlaying ?: throw IllegalArgumentException("Media is not ready.")

        // TODO - consider pre-loading bitmap
        val bitmap: Bitmap? = null

        val extras = bundleOf(
          MediaMetadataCompat.METADATA_KEY_ALBUM to bitmap,
          MediaMetadataCompat.METADATA_KEY_DISPLAY_ICON to bitmap
        )

        return MediaDescriptionCompat.Builder()
          .setMediaId(nowPlaying.id.toString())
          .setIconBitmap(bitmap)
          .setTitle(nowPlaying.title)
          .setDescription(nowPlaying.sourceUrl)
          .setExtras(extras)
          .build()
      }

    })

    connector.setPlayer(player)

    super.onCreate()
  }

  override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
    intent.extras
      ?.getString(Intent.EXTRA_TEXT)
      ?.let { launch { prepareAudio(it) } }
      ?: logInfo("Could not extract url from onStartCommand")
    return super.onStartCommand(intent, flags, startId)
  }

  private suspend fun prepareAudio(url: String) {
    val result = fetchSample(url)
    play(result)
  }

  private suspend fun fetchSample(url: String) =
      withContext(Dispatchers.IO) {
      sampleRepository.getSample(url)
    }

  private fun play(sample: MediaSample) {
    nowPlaying = sample.also { onSamplePrepared?.invoke(it) }

    // source for individual content
    val source = mediaSourceFactory.createMediaSource(Uri.parse(sample.streamUrl))

    // playlist source for multiple sources
    val concatenatingMediaSource = ConcatenatingMediaSource(source)

    player.run {
      prepare(concatenatingMediaSource)
      playWhenReady = true
    }
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

  inner class LocalBinder : Binder() {
    val service: MediaService
      get() = this@MediaService
  }
}


