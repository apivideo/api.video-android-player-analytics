package video.api.player.analytics

import java.io.IOException
import java.net.URL

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
 * A class that describes a video from api.video.
 * For custom domain, you must use this class.
 * For custom VOD domain or custom live domain, you must use [VideoInfo.fromMediaURL].
 *
 * @param videoId the video id
 * @param videoType the video type
 * @param collectorDomainURL the URL for player analytics collector. Only for if you use a custom collector domain.
 */
data class VideoInfo(
    val videoId: String,
    val videoType: VideoType,
    val collectorDomainURL: URL = URL(Options.DEFAULT_COLLECTOR_DOMAIN_URL)
) {
    /**
     * @param videoId the video id
     * @param videoType the video type
     * @param collectorDomainURL the URL for the player analytics collector. Only if you have a custom collector domain.
     */
    constructor(videoId: String, videoType: VideoType, collectorDomainURL: String) : this(
        videoId,
        videoType,
        URL(collectorDomainURL)
    )

    /**
     * The URL for player analytics collector
     */
    val pingUrl = "${collectorDomainURL}/${videoType.type}"

    companion object {
        /**
         * Creates a [VideoInfo] from a media URL.
         *
         * @param mediaUrl the media URL to parse
         * @param collectorDomainURL the URL for the player analytics collector. Only if you have a custom collector domain.
         */
        fun fromMediaURL(
            mediaUrl: URL,
            collectorDomainURL: URL = URL(Options.DEFAULT_COLLECTOR_DOMAIN_URL)
        ): VideoInfo {
            return Utils.parseMediaUrl(
                mediaUrl,
                collectorDomainURL
            )
        }

        /**
         * Creates a [VideoInfo] from a media URL.
         *
         * @param mediaUrl the media URL to parse
         * @param collectorDomainURL the URL for the player analytics collector. Only if you have a custom collector domain.
         */
        fun fromMediaURL(
            mediaUrl: String,
            collectorDomainURL: String = Options.DEFAULT_COLLECTOR_DOMAIN_URL
        ) = fromMediaURL(
            URL(mediaUrl),
            URL(collectorDomainURL),
        )
    }
}

/**
 * An option class to configure the player analytics.
 *
 * @param videoInfo the video info. For custom domains, you must use this API.
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
     * @param mediaUrl the api.video URL of your video (for example: `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`)
     * @param metadata the user metadata. See [metadata](https://api.video/blog/tutorials/dynamic-metadata).
     * @param onSessionIdReceived the callback called when session id has been received
     * @param onPing the callback called before sending [PlaybackPingMessage]
     */
    constructor(
        mediaUrl: String,
        metadata: Map<String, String> = emptyMap(),
        onSessionIdReceived: ((sessionId: String) -> Unit)? = null,
        onPing: ((message: PlaybackPingMessage) -> Unit)? = null
    ) : this(URL(mediaUrl), metadata, onSessionIdReceived, onPing)

    /**
     * @param mediaUrl the api.video URL of your video (for example: URL("https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8"))
     * @param metadata the user metadata. See [metadata](https://api.video/blog/tutorials/dynamic-metadata).
     * @param onSessionIdReceived the callback called when session id has been received
     * @param onPing the callback called before sending [PlaybackPingMessage]
     */
    constructor(
        mediaUrl: URL,
        metadata: Map<String, String> = emptyMap(),
        onSessionIdReceived: ((sessionId: String) -> Unit)? = null,
        onPing: ((message: PlaybackPingMessage) -> Unit)? = null
    ) : this(
        VideoInfo.fromMediaURL(mediaUrl),
        metadata,
        onSessionIdReceived,
        onPing
    )

    companion object {
        const val DEFAULT_COLLECTOR_DOMAIN_URL = "https://collector.api.video"
    }
}