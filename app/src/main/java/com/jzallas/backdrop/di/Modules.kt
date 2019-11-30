package com.jzallas.backdrop.di

import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.DefaultLoadControl
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.LoadControl
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory
import com.google.android.exoplayer2.extractor.ExtractorsFactory
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.trackselection.TrackSelector
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.jzallas.backdrop.R
import com.jzallas.backdrop.youtube.extractor.YouTubeExtractor
import com.jzallas.backdrop.youtube.api.LegacyYouTubeApi
import com.jzallas.backdrop.youtube.api.YouTubeUrlParser
import com.jzallas.backdrop.youtube.converter.UrlEncodedConverterFactory
import com.jzallas.backdrop.youtube.repository.MediaSampleRepository
import com.jzallas.backdrop.youtube.repository.VideoDetailRepository
import com.jzallas.backdrop.youtube.repository.YtFileRepository
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import org.koin.core.qualifier.named
import org.koin.dsl.module
import retrofit2.Retrofit
import com.jzallas.backdrop.moshi.LenientAdapterFactory


typealias MediaSourceFactory = AdsMediaSource.MediaSourceFactory
typealias MediaDescriptionAdapter = PlayerNotificationManager.MediaDescriptionAdapter
typealias NotificationListener = PlayerNotificationManager.NotificationListener

val playerModule = module {
  factory<ExoPlayer> {
    ExoPlayerFactory.newSimpleInstance(
      get(),
      get<RenderersFactory>(),
      get<TrackSelector>(),
      get<LoadControl>()
    ).apply { setAudioAttributes(get(), true) }
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

  single(named("userAgent")) { Util.getUserAgent(get(), "ExoPlayer") }

  factory<DataSource.Factory> { DefaultDataSourceFactory(get(), get<String>(named("userAgent"))) }

  factory<ExtractorsFactory> { DefaultExtractorsFactory() }

  factory<MediaSourceFactory> { ProgressiveMediaSource.Factory(get(), get()) }
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

val networkModule = module {
  factory { OkHttpClient() }

  factory {
    Retrofit.Builder()
      .client(get())
  }

  factory<LegacyYouTubeApi> {
    get<Retrofit.Builder>()
      .addConverterFactory(get(named("urlEncoded")))
      .baseUrl(LegacyYouTubeApi.BASE_URL)
      .build()
      .create(LegacyYouTubeApi::class.java)
  }

  factory { YouTubeExtractor(get()) }

  factory { YouTubeUrlParser() }
}

val repositoryModule = module {
  factory { MediaSampleRepository(get(), get()) }

  factory { YtFileRepository(get(), get()) }

  factory { VideoDetailRepository(get(), get(), get()) }
}

val parsingModule = module {
  single {
    Moshi.Builder()
      .add(get<LenientAdapterFactory>())
      .build()
  }

  single { LenientAdapterFactory() }

  single(named("urlEncoded")) { UrlEncodedConverterFactory.create() }
}
