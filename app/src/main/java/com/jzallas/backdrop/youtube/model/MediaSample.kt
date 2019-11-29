package com.jzallas.backdrop.youtube.model

data class MediaSample(
  val title: String,
  val sourceUrl: String,
  val streamUrl: String,
  val thumbnailUrl: String,
  val previewUrl: String
) {
  val id = 1 // TODO - consider real ids for playlist support
}
