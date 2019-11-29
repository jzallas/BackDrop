package com.jzallas.backdrop.extensions.glide

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.ui.PlayerNotificationManager

fun RequestBuilder<Bitmap>.into(callback: PlayerNotificationManager.BitmapCallback): Nothing? {
  into(object : CustomTarget<Bitmap>() {
    override fun onLoadCleared(placeholder: Drawable?) {
      callback.onBitmap(null)
    }

    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
      callback.onBitmap(resource)
    }

  })
  return null
}
