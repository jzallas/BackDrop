package com.jzallas.backdrop

import android.app.Application
import com.jzallas.backdrop.di.jsModule
import com.jzallas.backdrop.di.networkModule
import com.jzallas.backdrop.di.notificationModule
import com.jzallas.backdrop.di.parsingModule
import com.jzallas.backdrop.di.playerModule
import com.jzallas.backdrop.di.repositoryModule
import com.jzallas.backdrop.extensions.koin.modules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BackDropApplication : Application() {

  override fun onCreate() {
    super.onCreate()
    startKoin{
      androidLogger()
      androidContext(this@BackDropApplication)
      modules(
        playerModule,
        notificationModule,
        parsingModule,
        jsModule,
        networkModule,
        repositoryModule
      )
    }
  }
}
