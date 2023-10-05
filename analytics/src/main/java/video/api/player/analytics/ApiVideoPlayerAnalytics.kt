package video.api.player.analytics

import android.util.Log
import java.io.IOException
import java.util.Timer
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
        private const val TAG = "ApiVideoPlayerAnalytics"

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
     * To be called when user plays a video for the first time.
     *
     * @param eventTime the event time in second. Default is the current time.
     * @param onSuccess the callback called when the events were successfully sent.
     * @param onError the callback called when the events could not be send.
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun play(
        eventTime: Float = currentTime,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send play event: $error", error)
        }
    ) {
        schedule()
        addEventAt(Event.PLAY, eventTime)
        onSuccess()
    }

    /**
     * To be called when user resumes a paused video.
     *
     * @param eventTime the event time in second. Default is the current time.
     * @param onSuccess the callback called when the events were successfully sent.
     * @param onError the callback called when the events could not be send.
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun resume(
        eventTime: Float = currentTime,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send resume event: $error", error)
        }
    ) {
        schedule()
        addEventAt(Event.RESUME, eventTime)
        onSuccess()
    }

    /**
     * To be called when video is ready to play.
     *
     * @param eventTime the event time in second. Default is the current time.
     * @param onSuccess the callback called when the events were successfully sent.
     * @param onError the callback called when the events could not be send.
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun ready(
        eventTime: Float = currentTime,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send ready event: $error", error)
        }
    ) {
        addEventAt(Event.READY, eventTime)
        sendPing(buildPingPayload(), onSuccess, onError)
    }

    /**
     * To be called when playback is at the end of the video.
     *
     * @param eventTime the event time in second. Default is the current time.
     * @param onSuccess the callback called when the events were successfully sent.
     * @param onError the callback called when the events could not be send.
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun end(
        eventTime: Float = currentTime,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send end event: $error", error)
        }
    ) {
        unschedule()
        addEventAt(Event.END, eventTime)
        sendPing(buildPingPayload(), onSuccess, onError)
    }

    /**
     * To be called when video is being seek.
     *
     * @param from the seek start time in second
     * @param to the seek end time in second
     * @param onSuccess the callback called when the events were successfully sent.
     * @param onError the callback called when the events could not be send.
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun seek(
        from: Float,
        to: Float,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send seek event: $error", error)
        }
    ) {
        require(from >= 0f) { "from must be positive value but from=$from" }
        require(to >= 0f) { "to must be positive value but to=$to" }

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
        onSuccess()
    }

    /**
     * To be called when a video is paused.
     *
     * @param eventTime the event time in second. Default is the current time.
     * @param onSuccess the callback called when the events were successfully sent.
     * @param onError the callback called when the events could not be send.
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun pause(
        eventTime: Float = currentTime,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send pause event: $error", error)
        }
    ) {
        unschedule()
        addEventAt(Event.PAUSE, eventTime)
        sendPing(buildPingPayload(), onSuccess, onError)
    }

    /**
     * To be called when video will not be read again.
     *
     * @param eventTime the event time in second. Default is the current time.
     * @param onSuccess the callback called when the events were successfully sent.
     * @param onError the callback called when the events could not be send.
     * @return a [Future] result. Use it to check if an exception has happened.
     */
    fun destroy(
        eventTime: Float = currentTime,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = { error ->
            Log.e(TAG, "Failed to send destroy event: $error", error)
        }
    ) {
        unschedule()
        pause(eventTime, onSuccess, onError)
    }

    private fun addEventAt(eventName: Event, eventTime: Float) {
        eventsStack.add(PingEvent(type = eventName, at = eventTime))
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

    private fun sendPing(
        payload: PlaybackPingMessage,
        onSuccess: (() -> Unit) = {},
        onError: ((Exception) -> Unit) = {}
    ) {
        options.onPing?.let { it(payload) }
        RequestManager.sendPing(options.videoInfo.pingUrl, payload,
            {
                if (sessionId == null) {
                    sessionId = it
                }
                onSuccess()
            }, { error ->
                Log.e(TAG, "Failed to send payload $payload due to $error")
                onError(error)
            })
        eventsStack.clear()
    }
}