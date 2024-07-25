package video.api.player.analytics.exoplayer.example

object Utils {
    /**
     * Infers the video URL from the mediaId
     */
    fun inferManifestUrl(mediaId: String): String {
        return when {
            mediaId.startsWith("vi") -> "https://vod.api.video/vod/$mediaId/hls/manifest.m3u8"
            mediaId.startsWith("li") -> "https://live.api.video/$mediaId.m3u8"
            else -> throw IllegalArgumentException("Invalid mediaId")
        }
    }
}