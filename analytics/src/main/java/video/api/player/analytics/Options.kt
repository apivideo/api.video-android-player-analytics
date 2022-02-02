package video.api.player.analytics

import java.io.IOException
import java.util.regex.Pattern

enum class VideoType(val type: String) {
    LIVE("live"),
    VOD("vod")
}

fun String.toVideoType(): VideoType {
    VideoType.values().forEach {
        if (it.type == this) {
            return it
        }
    }
    throw IOException("Can't determine if video is vod or live.")
}

data class VideoInfo(val pingUrl: String, val videoId: String, val videoType: VideoType)

data class Options(
    val videoInfo: VideoInfo,
    val metadata: Map<String, String>,
    val onSessionIdReceived: ((sessionId: String) -> Unit)? = null,
    val onPing: ((message: PlaybackPingMessage) -> Unit)? = null
) {
    constructor(
        mediaUrl: String,
        metadata: Map<String, String>,
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