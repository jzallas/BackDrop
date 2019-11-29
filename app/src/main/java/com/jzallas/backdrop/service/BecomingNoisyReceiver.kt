package com.jzallas.backdrop.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

internal class BecomingNoisyReceiver(
  private val context: Context,
  session: MediaSessionCompat
) : BroadcastReceiver(), LifecycleObserver {

  private val controller = MediaControllerCompat(context, session.sessionToken)

  private var registered = false

  @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
  fun register() {
    if (!registered) {
      val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
      context.registerReceiver(this, filter)
      registered = true
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
  fun unregister() {
    if (registered) {
      context.unregisterReceiver(this)
      registered = false
    }
  }

  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
      controller.transportControls.pause()
    }
  }
}
