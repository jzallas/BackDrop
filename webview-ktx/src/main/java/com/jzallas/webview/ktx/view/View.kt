package com.jzallas.webview.ktx.view

import android.view.View

internal fun View.getOrSetTag(key: Int, default: () -> Any): Any {
  var tag = getTag(key)
  if (tag == null) {
    tag = default.invoke()
    setTag(key, tag)
  }
  return tag
}
