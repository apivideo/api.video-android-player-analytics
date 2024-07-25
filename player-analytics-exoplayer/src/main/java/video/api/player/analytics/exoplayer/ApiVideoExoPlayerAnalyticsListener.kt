package video.api.player.analytics.exoplayer

import android.os.Handler
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.android.asCoroutineDispatcher
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import video.api.player.analytics.core.ApiVideoPlayerAnalyticsAgent
import video.api.player.analytics.exoplayer.extensions.currentPositionInS
import video.api.player.analytics.exoplayer.extensions.playerAnalyticsErrorCode
import video.api.player.analytics.exoplayer.utils.Utils
import video.api.player.analytics.exoplayer.utils.tickerFlow
import video.api.player.analytics.core.payload.ErrorCode
import video.api.player.analytics.core.payload.Event
import java.io.Closeable
import kotlin.time.Duration.Companion.milliseconds

/**
 * An agent that sends events from ExoPlayer to the api.video analytics.
 * It is an [AnalyticsListener] that listens to the ExoPlayer and sends events to the api.video analytics.
 *
 * It will be closed when the player is released.
 *
 * @param exoPlayer The ExoPlayer instance to listen to.
 * @param collectorUrl The URL of the api.video analytics collector. Expected format: "https://collector.mycustomdomain.com".
 */
@OptIn(UnstableApi::class)
internal class ApiVideoExoPlayerAnalyticsListener(
    private val exoPlayer: ExoPlayer,
    collectorUrl: String? = null
) : AnalyticsListener,
    Closeable {
    private val analyticsAgent = ApiVideoPlayerAnalyticsAgent.create(collectorUrl)
    private var firstPlay = true

    private val dispatcher = Handler(exoPlayer.applicationLooper).asCoroutineDispatcher()
    private val scope = CoroutineScope(Dispatchers.Default)

    private var timeUpdateTickerFlow: Job? = null

    private fun scheduleTimeUpdate() {
        synchronized(this) {
            timeUpdateTickerFlow = tickerFlow(DEFAULT_UPDATE_TIME_INTERVAL_IN_MS.milliseconds)
                .onEach {
                    safeAddEvent(Event.EventType.TIME_UPDATE)
                }
                .flowOn(dispatcher)
                .launchIn(scope)
        }
    }

    private fun unscheduleTimeUpdate() {
        val tickerFlow = synchronized(this) {
            timeUpdateTickerFlow
        }
        tickerFlow?.cancel()
    }

    override fun close() {
        unscheduleTimeUpdate()
        analyticsAgent.close()
        scope.coroutineContext.cancel()
    }

    private fun safeAddEvent(eventType: Event.EventType) {
        try {
            addEvent(eventType)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to add event: $eventType", e)
        }
    }

    private fun addEvent(eventType: Event.EventType) {
        val videoSize = exoPlayer.videoSize
        val event =
            Event.createNow(
                eventType,
                exoPlayer.currentPositionInS,
                videoSize.width,
                videoSize.height,
                !exoPlayer.isPlaying,
                exoPlayer.playerError?.playerAnalyticsErrorCode ?: ErrorCode.NONE
            )

        analyticsAgent.addEvent(event)
    }

    override fun onIsPlayingChanged(
        eventTime: AnalyticsListener.EventTime,
        isPlaying: Boolean
    ) {
        safeAddEvent(if (isPlaying) Event.EventType.PLAY else Event.EventType.PAUSE)
        // Schedule or unschedule time update
        if (isPlaying) {
            scheduleTimeUpdate()
        } else {
            unscheduleTimeUpdate()
        }
    }

    override fun onPlayerError(
        eventTime: AnalyticsListener.EventTime,
        error: PlaybackException
    ) {
        safeAddEvent(Event.EventType.ERROR)
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        val eventType = when (state) {
            Player.STATE_READY -> {
                if (firstPlay) {
                    firstPlay = false
                    Event.EventType.LOADED
                } else {
                    return
                }
            }

            Player.STATE_ENDED -> {
                /**
                 * Sometimes, the player can be in the ENDED state without having played any media.
                 */
                if (exoPlayer.currentPositionInS > 0.0f) {
                    Event.EventType.END
                } else {
                    return
                }
            }

            else -> return
        }
        safeAddEvent(eventType)
    }

    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        when (reason) {
            Player.DISCONTINUITY_REASON_SEEK -> safeAddEvent(Event.EventType.SEEK)
            else -> return
        }
    }

    override fun onMediaItemTransition(
        eventTime: AnalyticsListener.EventTime,
        mediaItem: MediaItem?,
        reason: Int
    ) {
        if (mediaItem == null) return
        mediaItem.localConfiguration?.uri?.let {
            try {
                analyticsAgent.setMediaId(Utils.parseMediaUrl(it))
                safeAddEvent(Event.EventType.SRC)
            } catch (e: Exception) {
                analyticsAgent.disable()
                Log.e(TAG, "Failed to parse media URL: $it. Temporary disabling agent.")
            }
        }
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        close()
    }

    companion object {
        private const val TAG = "ExoPlayerAgent"

        private const val DEFAULT_UPDATE_TIME_INTERVAL_IN_MS = 250L
    }
}