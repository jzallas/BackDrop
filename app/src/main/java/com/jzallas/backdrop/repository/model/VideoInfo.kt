package com.jzallas.backdrop.repository.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoInfo(
  val formats: List<Format>,
  @SerialName("videoDetails") val details: Details,
  val thumbnails: List<Thumbnail>
)

@Serializable
data class Details(
  val title: String,
  val thumbnails: List<Thumbnail>
)

@Serializable
data class Format(
  val url: String,
  val mimeType: String? = null,
  val audioBitrate: Int? = null,
  val isHLS: Boolean,
  val isDashMPD: Boolean
)

@Serializable
data class Thumbnail(
  val width: Int,
  val height: Int,
  val url: String
)
