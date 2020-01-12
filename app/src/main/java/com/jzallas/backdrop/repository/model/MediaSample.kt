package com.jzallas.backdrop.repository.model

data class MediaSample(
  val title: String,
  val sourceUrl: String,
  val thumbnailUrl: String,
  val previewUrl: String
) {
  // this should hopefully be unique and predictable enough to represent an id
  val id = sourceUrl.hashCode()
}
