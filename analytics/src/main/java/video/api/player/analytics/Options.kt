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
         * @param vodDomainURL the URL for the vod domain. Only if you have a custom vod domain.
         * @param liveDomainURL the URL for the live domain. Only if you have a custom live domain.
         */
        fun fromMediaURL(
            mediaUrl: URL,
            collectorDomainURL: URL = URL(Options.DEFAULT_COLLECTOR_DOMAIN_URL),
            vodDomainURL: URL = URL(Options.DEFAULT_VOD_DOMAIN_URL),
            liveDomainURL: URL = URL(Options.DEFAULT_LIVE_DOMAIN_URL)
        ): VideoInfo {
            val vodDomainURLs = if (vodDomainURL.toString() == Options.DEFAULT_VOD_DOMAIN_URL) {
                listOf(vodDomainURL, URL(Options.DEFAULT_DEPRECATED_VOD_DOMAIN_URL))
            } else {
                listOf(vodDomainURL)
            }
            return Utils.parseMediaUrl(
                mediaUrl,
                collectorDomainURL,
                vodDomainURLs,
                liveDomainURL
            )
        }

        /**
         * Creates a [VideoInfo] from a media URL.
         *
         * @param mediaUrl the media URL to parse
         * @param collectorDomainURL the URL for the player analytics collector. Only if you have a custom collector domain.
         * @param vodDomainURL the URL for the vod domain. Only if you have a custom vod domain.
         * @param liveDomainURL the URL for the live domain. Only if you have a custom live domain.
         */
        fun fromMediaURL(
            mediaUrl: String,
            collectorDomainURL: String = Options.DEFAULT_COLLECTOR_DOMAIN_URL,
            vodDomainURL: String = Options.DEFAULT_VOD_DOMAIN_URL,
            liveDomainURL: String = Options.DEFAULT_LIVE_DOMAIN_URL
        ) = fromMediaURL(
            URL(mediaUrl),
            URL(collectorDomainURL),
            URL(vodDomainURL),
            URL(liveDomainURL)
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
        const val DEFAULT_VOD_DOMAIN_URL = "https://vod.api.video/vod"
        const val DEFAULT_DEPRECATED_VOD_DOMAIN_URL = "https://cdn.api.video/vod"
        const val DEFAULT_LIVE_DOMAIN_URL = "https://live.api.video"
    }
}