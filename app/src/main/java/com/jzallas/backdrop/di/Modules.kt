package com.jzallas.backdrop.di

import android.webkit.WebViewClient
import androidx.webkit.WebViewAssetLoader
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.MediaSourceFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.jzallas.backdrop.R
import com.jzallas.backdrop.webview.WebViewFactory
import com.jzallas.backdrop.repository.MediaSourceRepository
import org.koin.core.qualifier.named
import org.koin.dsl.module
import com.jzallas.backdrop.repository.YouTubeApi
import com.jzallas.backdrop.repository.YouTubeRepository
import com.jzallas.webview.client.DelegateWebViewClient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.dsl.bind


typealias MediaDescriptionAdapter = PlayerNotificationManager.MediaDescriptionAdapter
typealias NotificationListener = PlayerNotificationManager.NotificationListener

val playerModule = module {
  factory<ExoPlayer> {
    SimpleExoPlayer.Builder(get())
      .build()
      .apply {
        setAudioAttributes(get(), true)
        setHandleAudioBecomingNoisy(true)
        setWakeMode(C.WAKE_MODE_LOCAL)
      }
  }

  factory {
    AudioAttributes.Builder()
      .setUsage(C.USAGE_MEDIA)
      .setContentType(C.CONTENT_TYPE_MUSIC)
      .build()
  }

  factory<RenderersFactory> { DefaultRenderersFactory(get()) }

  factory<TrackSelector> { DefaultTrackSelector() }

  factory<LoadControl> { DefaultLoadControl() }

  factory<DataSource.Factory> { DefaultDataSourceFactory(get(), get<String>(named("userAgent"))) }

  factory<ExtractorsFactory> { DefaultExtractorsFactory() }

  factory { ProgressiveMediaSource.Factory(get(), get()) } bind MediaSourceFactory::class
}

val notificationModule = module {
  single(named("playbackChannelId")) { "com.jzallas.backdrop.playbackChannel" }

  single(named("playbackChannelName")) { R.string.playbackChannelName }

  single(named("playbackChannelDescription")) { R.string.playbackChannelDescription }

  single(named("playbackNotificationId")) { 0x100 }

  factory<PlayerNotificationManager> { (adapter: MediaDescriptionAdapter, listener: NotificationListener) ->
    PlayerNotificationManager.createWithNotificationChannel(
      get(),
      get(named("playbackChannelId")),
      get(named("playbackChannelName")),
      get(named("playbackChannelDescription")),
      get(named("playbackNotificationId")),
      adapter,
      listener
    )
  }
}

val webViewModule = module {
  factory {
    DelegateWebViewClient().apply {
      shouldInterceptRequest += { _, request -> get<WebViewAssetLoader>().shouldInterceptRequest(request.url) }
    }
  } bind WebViewClient::class

  factory {
    WebViewAssetLoader.Builder()
      .addPathHandler("/assets/", get<WebViewAssetLoader.AssetsPathHandler>())
      .build()
  }

  factory { WebViewAssetLoader.AssetsPathHandler(get()) }

  factory { WebViewFactory(get(), get(named("userAgent")), get()) }
}

val networkModule = module {
  single(named("userAgent")) {
    "Mozilla/5.0 (Windows NT 6.1; Win64; x64)AppleWebKit/537.36 (KHTML, like Gecko) Chrome/40.0.2214.115 Safari/537.3"
  }

  factory {
    YouTubeApi(
      factory = get(),
      asset = "index.html",
      origin = "https://www.youtube.com",
      json = get()
    )
  }
}

val repositoryModule = module {
  factory { MediaSourceRepository(get(), get()) }

  factory { YouTubeRepository(get()) }
}

val parsingModule = module {
  single { Json(get<JsonConfiguration>()) }

  single { JsonConfiguration.Stable.copy(strictMode = false) }
}
