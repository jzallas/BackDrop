package com.jzallas.backdrop.repository

import com.jzallas.backdrop.javascript.JavascriptEvaluator
import com.jzallas.backdrop.repository.model.VideoInfo
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

class YouTubeApi(
  private val evaluator: JavascriptEvaluator,
  private val json: Json
) {

  fun getVideoInfo(url: String): VideoInfo {
    return evaluator.call("getVideoInfo", json.toJson(String.serializer(), url).toString())
      .let { json.parse(VideoInfo.serializer(), it) }
  }
}
