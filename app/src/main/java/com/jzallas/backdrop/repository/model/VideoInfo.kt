package com.jzallas.backdrop.repository.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
  val baseUrl: String? = null,
  @SerialName("video_url") val url: String,
  @SerialName("video_id") val id: String,
  val title: String,
  val formats: List<Format>,
  val thumbnails: List<Thumbnail>
)

@Serializable
data class Format(
  val itag: Int,
  val url: String,
  val mimeType: String? = null,
  val bitrate: String? = null,
  val width: Int? = null,
  val height: Int? = null,
  val lastModified: String? = null,
  val contentLength: String? = null,
  val quality: String? = null,
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
