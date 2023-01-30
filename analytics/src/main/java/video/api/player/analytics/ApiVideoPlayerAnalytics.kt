package video.api.player.analytics

import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.concurrent.timerTask

/**
 * Main controls on player analytics.
 *
 * A [Timer] sends regularly list of the logged [Event].
 *
 * @param options the player analytics options
 */
class ApiVideoPlayerAnalytics(
    private val options: Options
) {
    companion object {
        private const val PLAYBACK_PING_DELAY = 10 * 1000L
    }

    private val eventsStack = mutableListOf<PingEvent>()
    private var timer: Timer? = null
    private val loadedAt: String = Utils.nowUtcToIso()
    internal var sessionId: String? = null
        set(value) {
            value?.let { session ->
                options.onSessionIdReceived?.let { it(session) }
                field = session
            }
        }

    /**
     * Get/Set player current time. This field must be updated as the same rate of the video frame rate.
     */
    var currentTime: Float = 0F
        /**
         * Set player current time
         *
         * @param value the player current time in second
         */
        set(value) {
            if (value >= 0) {
                field = value
            } else {
                throw IOException("currentTime must be positive value but currentTime=$value")
            }
        }

    /**
     * Calls for the first play of the video.
     *
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun play(eventTime: Float = currentTime): Future<Unit> {
        schedule()
        return addEventAt(Event.PLAY, eventTime)
    }

    /**
     * Resumes a paused video.
     *
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun resume(eventTime: Float = currentTime): Future<Unit> {
        schedule()
        return addEventAt(Event.RESUME, eventTime)
    }

    /**
     * Calls when video is ready to play.
     *
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun ready(eventTime: Float = currentTime): Future<Unit> {
        addEventAt(Event.READY, eventTime)
        return sendPing(buildPingPayload())
    }

    /**
     * Calls when video is over.
     *
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun end(eventTime: Float = currentTime): Future<Unit> {
        unschedule()
        addEventAt(Event.END, eventTime)
        return sendPing(buildPingPayload())
    }

    /**
     * Calls when video is being seek.
     *
     * @param from the seek start time in second
     * @param to the seek end time in second
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun seek(from: Float, to: Float): Future<Unit> {
        if ((from >= 0) && (to >= 0)) {
            eventsStack.add(
                PingEvent(
                    type = if (from < to) {
                        Event.SEEK_FORWARD
                    } else {
                        Event.SEEK_BACKWARD
                    },
                    from = from,
                    to = to
                )
            )
            return CompletableFuture.completedFuture(Unit)
        } else {
            throw IOException("from and to must be positive value but from=$from to=$to")
        }
    }

    /**
     * Calls when video is paused.
     *
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun pause(eventTime: Float = currentTime): Future<Unit> {
        unschedule()
        addEventAt(Event.PAUSE, eventTime)
        return sendPing(buildPingPayload())
    }

    /**
     * Calls when video will not be read again.
     *
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun destroy(eventTime: Float = currentTime): Future<Unit> {
        unschedule()
        return pause(eventTime)
    }

    private fun addEventAt(eventName: Event, eventTime: Float): Future<Unit> {
        eventsStack.add(PingEvent(type = eventName, at = eventTime))
        return CompletableFuture.completedFuture(Unit)
    }

    private fun schedule() {
        synchronized(this) {
            if (timer == null) {
                timer = Timer().apply {
                    scheduleAtFixedRate(timerTask {
                        sendPing(buildPingPayload())
                    }, PLAYBACK_PING_DELAY, PLAYBACK_PING_DELAY)
                }
            }
        }
    }

    private fun unschedule() {
        synchronized(this) {
            timer?.cancel()
            timer = null
        }
    }

    private fun buildPingPayload(): PlaybackPingMessage {
        val session = when (options.videoInfo.videoType) {
            VideoType.LIVE -> Session.buildLiveStreamSession(
                sessionId = sessionId,
                loadedAt = loadedAt,
                videoId = options.videoInfo.videoId,
                metadata = options.metadata,
                referrer = ""
            )
            VideoType.VOD -> Session.buildVodSession(
                sessionId = sessionId,
                loadedAt = loadedAt,
                videoId = options.videoInfo.videoId,
                metadata = options.metadata,
                referrer = ""
            )
        }
        return PlaybackPingMessage(
            session = session,
            events = eventsStack
        )
    }

    private fun sendPing(payload: PlaybackPingMessage): Future<Unit> {
        options.onPing?.let { it(payload) }
        val future = CompletableFuture<Unit>()
        RequestManager.sendPing(options.videoInfo.pingUrl, payload,
            {
                if (sessionId == null) {
                    sessionId = it
                }
                future.complete(Unit)
            }, { error ->
                future.completeExceptionally(error)
            })
        eventsStack.clear()
        return future
    }
}