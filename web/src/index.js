const { getInfo } = require('ytdl-core')

async function getVideoInfo(url) {
    var info = await getInfo(url)

    var results = {
        ...info,
        thumbnails: info.player_response.videoDetails.thumbnail.thumbnails
    }

    // original youtube response -- don't care about this
    delete results.player_response

    return results
}

module.exports = { getVideoInfo }

if (typeof window !== 'undefined') {
    Object.keys(module.exports)
        .forEach(key => window[key] = module.exports[key])
}
