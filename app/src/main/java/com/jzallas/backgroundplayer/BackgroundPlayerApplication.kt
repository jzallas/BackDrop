package com.jzallas.backgroundplayer

import android.app.Application
import com.jzallas.backgroundplayer.di.networkModule
import com.jzallas.backgroundplayer.di.notificationModule
import com.jzallas.backgroundplayer.di.parsingModule
import com.jzallas.backgroundplayer.di.playerModule
import com.jzallas.backgroundplayer.di.repositoryModule
import com.jzallas.backgroundplayer.extensions.koin.modules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BackgroundPlayerApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin{
      androidLogger()
      androidContext(this@BackgroundPlayerApplication)
      modules(
        playerModule,
        notificationModule,
        parsingModule,
        networkModule,
        repositoryModule
      )
    }
  }
}
