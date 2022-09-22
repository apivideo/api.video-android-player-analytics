package video.api.player.analytics

import java.io.IOException
import java.util.regex.Pattern

/**
 * The api.video video type.
 */
enum class VideoType(val type: String) {
    /** Video is a live stream */
    LIVE("live"),

    /** Video is a video on demand */
    VOD("vod")
}

/**
 * Converts String to a [VideoType].
 *
 * @return corresponding [VideoType] or an exception
 */
fun String.toVideoType(): VideoType {
    VideoType.values().forEach {
        if (it.type == this) {
            return it
        }
    }
    throw IOException("Can't determine if video is vod or live.")
}

/**
 * Describes a video.
 *
 * @param pingUrl the URL for player analytics collector
 * @param videoId the video id
 * @param videoType the video type
 */
data class VideoInfo(val pingUrl: String, val videoId: String, val videoType: VideoType)

/**
 * Player analytics options.
 *
 * @param videoInfo the video info
 * @param metadata the user metadata. See [metadata](https://api.video/blog/tutorials/dynamic-metadata).
 * @param onSessionIdReceived the callback called when session id has been received
 * @param onPing the callback called before sending [PlaybackPingMessage]
 */
data class Options(
    val videoInfo: VideoInfo,
    val metadata: Map<String, String> = emptyMap(),
    val onSessionIdReceived: ((sessionId: String) -> Unit)? = null,
    val onPing: ((message: PlaybackPingMessage) -> Unit)? = null
) {
    /**
     * @param mediaUrl the api.video URL of your URL (for example: `https://cdn.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`)
     * @param metadata the user metadata. See [metadata](https://api.video/blog/tutorials/dynamic-metadata).
     * @param onSessionIdReceived the callback called when session id has been received
     * @param onPing the callback called before sending [PlaybackPingMessage]
     */
    constructor(
        mediaUrl: String,
        metadata: Map<String, String> = emptyMap(),
        onSessionIdReceived: ((sessionId: String) -> Unit)? = null,
        onPing: ((message: PlaybackPingMessage) -> Unit)? = null
    ) : this(parseMediaUrl(mediaUrl), metadata, onSessionIdReceived, onPing)

    companion object {
        private fun parseMediaUrl(mediaUrl: String): VideoInfo {
            val regex =
                "https:/.*[/](vod|live)([/]|[/.][^/]*[/])([^/^.]*)[/.].*"
            val pattern = Pattern.compile(regex)
            val matcher = pattern.matcher(mediaUrl)

            if (matcher.groupCount() < 3) {
                throw IOException("The media url doesn't look like an api.video URL.")
            }

            try {
                matcher.find()

                val videoType =
                    matcher.group(1)?.toVideoType() ?: throw IOException("Failed to get video type")
                val videoId = matcher.group(3) ?: throw IOException("Failed to get videoId")

                return VideoInfo(
                    "https://collector.api.video/${videoType.type}",
                    videoId,
                    videoType
                )
            } catch (e: Exception) {
                e.message?.let {
                    throw IOException("The media url doesn't look like an api.video URL: $it")
                } ?: throw IOException("The media url doesn't look like an api.video URL.")
            }
        }
    }
}