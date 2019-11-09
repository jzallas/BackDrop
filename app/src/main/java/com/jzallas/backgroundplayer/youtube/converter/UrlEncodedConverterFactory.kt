package com.jzallas.backgroundplayer.youtube.converter

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class UrlEncodedConverterFactory : Converter.Factory() {
  companion object {
    fun create(): Converter.Factory = UrlEncodedConverterFactory()
  }

  override fun responseBodyConverter(
    type: Type,
    annotations: Array<Annotation>,
    retrofit: Retrofit
  ): Converter<ResponseBody, *>? {
    return when (type) {
      UrlEncodedBody::class.java -> UrlEncodedConverter
      else -> super.responseBodyConverter(type, annotations, retrofit)
    }
  }
}

object UrlEncodedConverter : Converter<ResponseBody, UrlEncodedBody> {
  override fun convert(value: ResponseBody): UrlEncodedBody? {
    val body = value.string()

    // this full url isn't used for anything
    // we're just leveraging the awesome query param canonicalization of OkHttp
    fun prepareQuery() = "https://www.example.com".toHttpUrl()
      .newBuilder()
      .encodedQuery(body)
      .build()

    fun HttpUrl.toMap() = (0 until querySize)
      .associateBy ({ queryParameterName(it)}, {queryParameterValue(it) })

    return runCatching { prepareQuery() }
      .mapCatching { it.toMap() }
      .map { UrlEncodedBody.Success(it) }
      .getOrDefault(UrlEncodedBody.Failure(body))
  }
}

sealed class UrlEncodedBody {
  data class Success(val map: Map<String, String?>) : UrlEncodedBody()
  data class Failure(val response: String) : UrlEncodedBody()
}
