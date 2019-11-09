package com.jzallas.backgroundplayer.youtube.api

import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import java.util.regex.Pattern

data class YouTubeUrl(val source: HttpUrl) {
  companion object {
    private const val INDEX_VIDEO_ID = 3
    private val LINK =
      Pattern.compile("(http|https)://(www\\.|m.|)youtube\\.com/watch\\?v=(.+?)( |\\z|&)")
    private val SHORT_LINK =
      Pattern.compile("(http|https)://(www\\.|)youtu.be/(.+?)( |\\z|&)")
  }

  constructor(url: String) : this(url.toHttpUrl())

  val videoId: String by lazy {
    val url = source.toString()

    listOf(
      LINK,
      SHORT_LINK
    )
      .asSequence()
      .map { it.matcher(url) }
      .filter { it.find() }
      .map { it.group(INDEX_VIDEO_ID) }
      .firstOrNull()
      ?: url.takeIf { it.matches("\\p{Graph}+?".toRegex()) }
      ?: throw IllegalStateException("Trying to extract a videoId from a non YouTube url: $url")
  }

  override fun toString() = source.toString()
}

class YouTubeUrlParser {
  fun parse(url: String) = YouTubeUrl(url)
}

