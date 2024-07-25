package video.api.player.analytics.exoplayer.utils

import android.net.Uri
import java.io.IOException
import java.util.regex.Pattern

internal object Utils {
    /**
     * Parse a media URL such as `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8` to retrieve the mediaId `vi5oDagRVJBSKHxSiPux5rYD`.
     * @param mediaUrl the media URL to parse
     *
     * @return the mediaId
     */
    internal fun parseMediaUrl(
        mediaUrl: Uri,
    ): String {
        /**
         * Group naming is not supported before Android API 26 and crashes
         * on very old version such as Android API 21
         */
        val regex = "https://[^/]+/(?>(vod|live)/)?(?>.*/)?((vi|li)[^/^.]*).*"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(mediaUrl.toString())

        if (matcher.groupCount() < 3) {
            throw IOException("The media url doesn't look like an api.video URL.")
        }

        try {
            matcher.find()
            return matcher.group(2) ?: throw IllegalArgumentException("Failed to get mediaId")
        } catch (e: Exception) {
            throw IllegalArgumentException("Failed to parse url $mediaUrl", e)
        }
    }
}