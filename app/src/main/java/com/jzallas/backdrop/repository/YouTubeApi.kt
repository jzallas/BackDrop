package com.jzallas.backdrop.repository

import com.jzallas.backdrop.javascript.JavascriptEvaluator
import com.jzallas.backdrop.repository.model.VideoInfo
import kotlinx.serialization.json.Json

class YouTubeApi(
  private val evaluator: JavascriptEvaluator,
  private val json: Json
) {

  fun getVideoInfo(url: String): VideoInfo {
    return evaluator.call("window.app.getVideoInfo", "'$url'")
      .let { json.parse(VideoInfo.serializer(), it) }
  }
}
