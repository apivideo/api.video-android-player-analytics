package video.api.analytics.exoplayer

import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
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
     */
    constructor(
        player: ExoPlayer,
        mediaUrl: URL
    ) : this(
        player,
        VideoInfo.fromMediaURL(
            mediaUrl
        )
    )

    /**
     * Creates a new instance of [ApiVideoAnalyticsListener].
     *
     * @param player the [ExoPlayer]
     * @param mediaUrl the api.video URL of your video (for example: `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`)
     */
    constructor(
        player: ExoPlayer,
        mediaUrl: String
    ) : this(
        player,
        URL(mediaUrl)
    )

    private var firstPlay = true
    private var isReady = false

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
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

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        analytics.destroy(eventTime.toSeconds())
    }
}