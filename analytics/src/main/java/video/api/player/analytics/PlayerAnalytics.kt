package video.api.player.analytics

import android.content.Context
import com.android.volley.Request
import com.android.volley.toolbox.Volley
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.concurrent.timerTask

open class PlayerAnalytics(context: Context, private val options: Options) {
    companion object {
        private const val PLAYBACK_PING_DELAY = 10 * 1000L
    }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { explicitNulls = false }
    private val eventsStack = mutableListOf<PingEvent>()
    private var timer: Timer? = null
    private val loadedAt: String = Utils.nowUtcToIso()
    internal var sessionId: String? = null
        set(value) {
            value?.let {
                options.onSessionIdReceived?.let { it(value) }
                field = value
            }
        }
    private val queue = Volley.newRequestQueue(context).apply {
        start()
    }

    var currentTime: Float = 0F
        set(value) {
            if (value >= 0) {
                field = value
            } else {
                throw IOException("currentTime must be positive value but currentTime=$value")
            }
        }

    fun play(): Future<Unit> {
        schedule()
        return addEventAt(Event.PLAY)
    }

    fun resume(): Future<Unit> {
        schedule()
        return addEventAt(Event.RESUME)
    }

    fun ready(): Future<Unit> {
        addEventAt(Event.READY)
        return sendPing(buildPingPayload())
    }

    fun end(): Future<Unit> {
        unschedule()
        addEventAt(Event.END)
        return sendPing(buildPingPayload())
    }

    fun seek(from: Float, to: Float): Future<Unit> {
        if ((from > 0) && (to > 0)) {
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

    fun pause(): Future<Unit> {
        unschedule()
        addEventAt(Event.PAUSE)
        return sendPing(buildPingPayload())
    }

    fun destroy(): Future<Unit> {
        unschedule()
        return pause()
    }

    private fun addEventAt(eventName: Event): Future<Unit> {
        eventsStack.add(PingEvent(type = eventName, at = currentTime))
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
        val stringRequest = StringRequest(
            Request.Method.POST,
            options.videoInfo.pingUrl,
            json.encodeToString(payload),
            { response ->
                try {
                    val jsonResponse = Json.parseToJsonElement(response).jsonObject
                    jsonResponse["session"]?.let {
                        val sessionId = it.jsonPrimitive.content
                        if (this.sessionId == null) {
                            this.sessionId = sessionId
                        }
                    }
                    future.complete(Unit)
                } catch (e: Exception) {
                    future.completeExceptionally(e)
                }
            },
            { error ->
                future.completeExceptionally(error)
            })

        eventsStack.clear()
        queue.add(stringRequest)
        return future
    }
}