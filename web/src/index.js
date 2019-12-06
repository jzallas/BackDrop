const { getInfo, filterFormats } = require('ytdl-core')

if (!window.app) {
    window.app = {}
}

window.app.getVideoInfo = (id, url) => {
    getInfo(url, (err, info) => {
      if (err) {
        android.onFailure(id, JSON.stringify(err));
        return
      }

      var results = {
      ...info,
      thumbnails: info.player_response.videoDetails.thumbnail.thumbnails
     }

      // original youtube response -- don't care about this
      delete results.player_response

      android.onSuccess(id, JSON.stringify(results))
    })
  }
