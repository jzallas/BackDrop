const { getBasicInfo, filterFormats } = require('ytdl-core');

window.app.getVideoInfo = function(id, url) {
    getBasicInfo(url, function(err, info) {
      if (err) {
        android.onFailure(id, JSON.stringify(err));
        return;
      }

      var results = { ...info };

      // original youtube response -- don't care about this
      delete results.player_response;

      android.onSuccess(id, JSON.stringify(results));
    });
  }
