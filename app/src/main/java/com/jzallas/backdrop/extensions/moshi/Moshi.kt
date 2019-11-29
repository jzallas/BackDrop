package com.jzallas.backdrop.extensions.moshi

import com.squareup.moshi.Moshi

inline fun <reified T> Moshi.deserialize(json: String): T? {
  return adapter(T::class.java).fromJson(json)
}
