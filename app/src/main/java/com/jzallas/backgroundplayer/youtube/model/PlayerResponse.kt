package com.jzallas.backgroundplayer.youtube.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class PlayerResponse(
  val videoDetails: VideoDetails
)

@JsonClass(generateAdapter = true)
data class VideoDetails(
  @Json(name = "videoId") val id: String,
  val title: String,
  val lengthSeconds: Long,
  val keywords: List<String> = emptyList(),
  val channelId: String,
  val shortDescription: String,
  @Json(name = "thumbnail") val thumbnails: Thumbnails,
  val author: String,
  val averageRating: Float,
  val viewCount: Long
)

@JsonClass(generateAdapter = true)
data class Thumbnails(val thumbnails: List<Thumbnail>): Iterable<Thumbnail> by thumbnails

@JsonClass(generateAdapter = true)
data class Thumbnail(
  val url: String,
  val width: Int,
  val height: Int
)
