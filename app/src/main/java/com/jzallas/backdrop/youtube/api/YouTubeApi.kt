package com.jzallas.backdrop.youtube.api

import com.jzallas.backdrop.youtube.converter.UrlEncodedBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface YouTubeApi {

  companion object {
    const val BASE_URL = "https://www.youtube.com"
  }

  @GET("/get_video_info?el=embedded&ps=default&eurl=&gl=US&hl=en")
  fun getInfo(@Query("video_id") videoId: String): Call<UrlEncodedBody>

  @GET("/get_video_info?el=detailpage&ps=default&eurl=&gl=US&hl=en")
  fun getDetails(@Query("video_id") videoId: String): Call<UrlEncodedBody>
}
