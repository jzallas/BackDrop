package com.jzallas.backdrop.repository

import com.jzallas.backdrop.javascript.JavascriptEvaluator

class YouTubeApi(private val evaluator: JavascriptEvaluator) {
  fun getVideoInfo(url: String): String {
    return evaluator.call("window.app.getVideoInfo", "'$url'")
  }
}
