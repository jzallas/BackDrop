package com.jzallas.backdrop.youtube.repository

import com.jzallas.backdrop.extensions.log.logError
import com.jzallas.backdrop.extensions.moshi.deserialize
import com.jzallas.backdrop.youtube.api.YouTubeApi
import com.jzallas.backdrop.youtube.api.YouTubeUrlParser
import com.jzallas.backdrop.youtube.converter.UrlEncodedBody
import com.jzallas.backdrop.youtube.model.PlayerResponse
import com.jzallas.backdrop.youtube.model.VideoDetails
import com.squareup.moshi.Moshi
import retrofit2.Response

class VideoDetailRepository (
  private val api: YouTubeApi,
  private val urlParser: YouTubeUrlParser,
  private val moshi: Moshi
) {

  companion object {
    private const val KEY_PLAYER_METADATA = "player_response"
  }

  fun getDetails(url: String) : VideoDetails {
    val videoId = urlParser.parse(url).videoId

    fun getInfo() = api.getInfo(videoId)
      .execute()
      .let(::extractMetadata)

    fun getDetails() = api.getDetails(videoId)
      .execute()
      .let(::extractMetadata)

    return runCatching { getInfo() }
      .onFailure { logError("Failed to fetch info, will try to fetch details instead.", it) }
      .recoverCatching { getDetails() }
      .getOrThrow()
  }

  private fun extractMetadata(response: Response<UrlEncodedBody>?): VideoDetails {
    response ?: throw IllegalStateException("Failed to fetch metadata. Response unavailable.")

    return when(val body = response.bodyOrError()) {
      is UrlEncodedBody.Failure -> throw IllegalStateException("Unable to parse metadata: ${body.response}")
      is UrlEncodedBody.Success -> {
        val json = body.map[KEY_PLAYER_METADATA]
          ?: throw IllegalStateException("Failed to fetch metadata: unable to find ${KEY_PLAYER_METADATA}.")

        return runCatching { moshi.deserialize<PlayerResponse>(json)!! }
          .map { it.videoDetails }
          .getOrElse { throw IllegalStateException("Failed to deserialize ${KEY_PLAYER_METADATA}: $json", it) }
      }
    }
  }

  private fun <T> Response<T>.bodyOrError() : T {
    return body().takeIf { isSuccessful }
      ?: throw IllegalStateException("Failed to fetch metadata. [Response ${code()}] : ${errorBody()}")
  }
}
