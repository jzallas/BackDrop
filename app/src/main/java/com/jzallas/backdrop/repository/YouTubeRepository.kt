package com.jzallas.backdrop.repository

import com.jzallas.backdrop.repository.model.VideoInfo

class YouTubeRepository(private val api: YouTubeApi) {
  fun getVideoInfo(url: String) : VideoInfo {
    return api.getVideoInfo(url)
  }
}
