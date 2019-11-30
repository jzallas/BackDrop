package com.jzallas.backdrop.repository

class YouTubeRepository(private val api: YouTubeApi) {
  fun getVideoInfo(url: String) : String {
    return api.getVideoInfo(url)
  }
}
