package com.jzallas.backdrop.moshi

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class LenientAdapterFactory : JsonAdapter.Factory {
  override fun create(type: Type, annotations: MutableSet<out Annotation>, moshi: Moshi): JsonAdapter<*>? {
    return moshi.nextAdapter<Any>(this, type, annotations).lenient()
  }
}
