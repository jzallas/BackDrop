package com.jzallas.backdrop

import android.content.ComponentName
import android.content.Context
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProviders
import com.bumptech.glide.Glide
import com.jzallas.backdrop.extensions.cast
import com.jzallas.backdrop.main.MainViewModel
import com.jzallas.backdrop.repository.model.MediaSample
import com.jzallas.backdrop.service.MediaService
import kotlinx.android.synthetic.main.main_activity.*

class MainActivity : AppCompatActivity(), ServiceConnection {

  private lateinit var viewModel: MainViewModel

  private var service: MediaService? = null

  // since the service cannot receive this intent directly,
  // we will proxy the original intent to the service
  private val mediaServiceIntent by lazy { MediaService.createIntent(this, intent) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.main_activity)

    viewModel = ViewModelProviders.of(this).get(MainViewModel::class.java)

    ActivityCompat.startForegroundService(this, mediaServiceIntent)
  }

  override fun onStart() {
    super.onStart()
    bindService(mediaServiceIntent, this, Context.BIND_AUTO_CREATE)
  }

  override fun onStop() {
    service?.let {
      it.onSamplePrepared = null
      unbindService(this)
      service = null
    }
    super.onStop()
  }

  override fun onServiceDisconnected(name: ComponentName?) {
    playerControlView.player = null
  }

  override fun onServiceConnected(name: ComponentName, binder: IBinder) {
    binder.cast<MediaService.LocalBinder>()
      .service
      .apply { playerControlView.player = player }
      .apply { onSamplePrepared = ::renderSample }
      .also { this.service = it }
  }

  private fun renderSample(sample: MediaSample) {
    titleView.text = sample.title
    subtitleView.text = sample.sourceUrl
    Glide.with(this)
      .load(sample.previewUrl)
      .into(previewImageView)
  }
}
