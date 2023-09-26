package video.api.analytics.exoplayer

import android.util.Log
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
    private val analytics: ApiVideoPlayerAnalytics,
    private val onError: (Exception) -> Unit
) :
    AnalyticsListener {

    /**
     * Creates a new instance of [ApiVideoAnalyticsListener].
     *
     * @param player the [ExoPlayer]
     * @param videoInfo the video info.
     * @param onError the callback called when the events could not be send.
     */
    constructor(
        player: ExoPlayer,
        videoInfo: VideoInfo,
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send event", error)
        }
    ) : this(
        player,
        ApiVideoPlayerAnalytics(Options(videoInfo = videoInfo)),
        onError
    )

    /**
     * Creates a new instance of [ApiVideoAnalyticsListener].
     *
     * @param player the [ExoPlayer]
     * @param mediaUrl the api.video URL of your video (for example: `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`)
     * @param onError the callback called when the events could not be send.
     */
    constructor(
        player: ExoPlayer,
        mediaUrl: URL,
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send event", error)
        }
    ) : this(
        player,
        VideoInfo.fromMediaURL(
            mediaUrl
        ),
        onError
    )

    /**
     * Creates a new instance of [ApiVideoAnalyticsListener].
     *
     * @param player the [ExoPlayer]
     * @param mediaUrl the api.video URL of your video (for example: `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8`)
     * @param onError the callback called when the events could not be send.
     */
    constructor(
        player: ExoPlayer,
        mediaUrl: String,
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send event", error)
        }
    ) : this(
        player,
        URL(mediaUrl),
        onError
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
                analytics.play(eventTime.toSeconds(), onError = onError)
                firstPlay = false
            } else {
                analytics.resume(eventTime.toSeconds(), onError = onError)
            }
        } else {
            if (player.playbackState != Player.STATE_ENDED) {
                analytics.pause(eventTime.toSeconds(), onError = onError)
            }
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        if (state == Player.STATE_READY) {
            if (!isReady) {
                analytics.ready(eventTime.toSeconds(), onError = onError)
                isReady = true
            }
        } else if (state == Player.STATE_ENDED) {
            analytics.end(eventTime.toSeconds(), onError = onError)
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
                newPosition.positionMs.toSeconds(),
                onError = onError
            )
        }
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        analytics.destroy(eventTime.toSeconds(), onError = onError)
    }

    companion object {
        private const val TAG = "ApiVideoAnalyticsListen"
    }
}