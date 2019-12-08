package com.jzallas.backdrop.repository.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
  val baseUrl: String? = null,
  @SerialName("video_url") val url: String,
  @SerialName("video_id") val id: String,
  val media: Media,
  val title: String,
  val formats: List<Format>,
  val thumbnails: List<Thumbnail>
)

@Serializable
data class Media(
  val image: String? = null,
  val category: String,
  @SerialName("category_url") val categoryUrl: String,
  val game: String? = null,
  @SerialName("game_url") val gameUrl: String? = null,
  val year: Int? = null,
  val song: String? = null,
  val artist: String? = null,
  @SerialName("artist_url") val artistUrl: String? = null,
  val writers: String? = null,
  val licensed_by: String? = null
)

@Serializable
data class Author(
  val id: String,
  val name: String,
  val avatar: String,
  val verified: Boolean,
  val user: String,
  @SerialName("channel_url") val channelUrl: String,
  @SerialName("user_url") val userUrl: String
)

@Serializable
data class Format(
  val itag: Int,
  val url: String,
  val mimeType: String? = null,
  val bitrate: String? = null,
  val width: Int? = null,
  val height: Int? = null,
  val lastModified: String,
  val contentLength: String? = null,
  val quality: String,
  val qualityLabel: String? = null,
  val fps: Int? = null,
  val averageBitrate: Int? = null,
  val audioQuality: String? = null,
  val audioBitrate: Int? = null,
  val approxDurationMs: String? = null,
  val audioSampleRate: String? = null,
  val audioChannels: Int? = null,
  val container: String,
  val codecs: String,
  @SerialName("live") val isLive: Boolean,
  val isHLS: Boolean,
  val isDashMPD: Boolean
)

@Serializable
data class Thumbnail(
  val width: Int,
  val height: Int,
  val url: String
)