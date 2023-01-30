package video.api.analytics.exoplayer

import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.analytics.AnalyticsListener
import video.api.player.analytics.ApiVideoPlayerAnalytics
import video.api.player.analytics.Options

/**
 * api.video implementation of [AnalyticsListener] for ExoPlayer.
 *
 * Use it with [ExoPlayer.addAnalyticsListener] and remove it with [ExoPlayer.removeAnalyticsListener].
 *
 * @param context the application context
 * @param player the [ExoPlayer]
 * @param url the url of the video on api.video
 */
class ApiVideoAnalyticsListener(
    private val player: ExoPlayer,
    url: String
) :
    AnalyticsListener {
    private val analytics = ApiVideoPlayerAnalytics(Options(mediaUrl = url))
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