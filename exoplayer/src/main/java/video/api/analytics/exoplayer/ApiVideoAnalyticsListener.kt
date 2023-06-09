package video.api.analytics.exoplayer

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.AnalyticsListener
import video.api.player.analytics.ApiVideoPlayerAnalytics
import video.api.player.analytics.Options
import video.api.player.analytics.VideoInfo
import java.net.URL

/**
 * api.video implementation of [AnalyticsListener] for ExoPlayer.
 *
 * Use it with [ExoPlayer.addAnalyticsListener] and remove it with [ExoPlayer.removeAnalyticsListener].
 */
class ApiVideoAnalyticsListener
private constructor(
    private val player: ExoPlayer,
    private val analytics: ApiVideoPlayerAnalytics
) :
    AnalyticsListener {

    /**
     * Creates a new instance of [ApiVideoAnalyticsListener].
     *
     * @param player the [ExoPlayer]
     * @param videoInfo the video info.
     */
    constructor(
        player: ExoPlayer,
        videoInfo: VideoInfo
    ) : this(
        player,
        ApiVideoPlayerAnalytics(Options(videoInfo = videoInfo))
    )

    /**
     * Creates a new instance of [ApiVideoAnalyticsListener].
     *
     * @param player the [ExoPlayer]
     * @param mediaUrl the api.video URL of your video (for example: `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`)
     * @param vodDomainURL the URL for the vod domain. Only if you have a custom vod domain.
     * @param liveDomainURL the URL for the live domain. Only if you have a custom live domain.
     */
    constructor(
        player: ExoPlayer,
        mediaUrl: URL,
        vodDomainURL: URL = URL(Options.DEFAULT_VOD_DOMAIN_URL),
        liveDomainURL: URL = URL(Options.DEFAULT_LIVE_DOMAIN_URL)
    ) : this(
        player,
        VideoInfo.fromMediaURL(
            mediaUrl,
            vodDomainURL,
            liveDomainURL
        )
    )

    /**
     * Creates a new instance of [ApiVideoAnalyticsListener].
     *
     * @param player the [ExoPlayer]
     * @param mediaUrl the api.video URL of your video (for example: `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`)
     * @param vodDomainURL the URL for the vod domain. Only if you have a custom vod domain.
     * @param liveDomainURL the URL for the live domain. Only if you have a custom live domain.
     */
    constructor(
        player: ExoPlayer,
        mediaUrl: String,
        vodDomainURL: String = Options.DEFAULT_VOD_DOMAIN_URL,
        liveDomainURL: String = Options.DEFAULT_LIVE_DOMAIN_URL
    ) : this(
        player,
        URL(mediaUrl),
        URL(vodDomainURL),
        URL(liveDomainURL)
    )

    private var firstPlay = true
    private var isReady = false

    override fun onIsPlayingChanged(
        eventTime: AnalyticsListener.EventTime,
        isPlaying: Boolean
    ) {
        if (isPlaying) {
            if (firstPlay) {
                analytics.play(eventTime.toSeconds())
                firstPlay = false
            } else {
                analytics.resume(eventTime.toSeconds())
            }
        } else {
            if (player.playbackState != Player.STATE_ENDED) {
                analytics.pause(eventTime.toSeconds())
            }
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == Player.STATE_READY) {
            if (!isReady) {
                analytics.ready(eventTime.toSeconds())
                isReady = true
            }
        } else if (state == Player.STATE_ENDED) {
            analytics.end(eventTime.toSeconds())
        }
    }

    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        if (reason == Player.DISCONTINUITY_REASON_SEEK) {
            analytics.seek(
                oldPosition.positionMs.toSeconds(),
                newPosition.positionMs.toSeconds()
            )
        }
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        analytics.destroy(eventTime.toSeconds())
    }
}