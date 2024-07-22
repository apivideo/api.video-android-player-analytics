package video.api.player.analytics.core

import android.util.Log
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.UserAgent
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import video.api.player.analytics.core.payload.Batch
import video.api.player.analytics.core.payload.Event
import video.api.player.analytics.core.utils.FixedSizeListWithUpsert
import video.api.player.analytics.core.utils.PlayerAnalyticsUserAgent
import video.api.player.analytics.core.utils.UUID
import video.api.player.analytics.core.utils.tickerFlow
import java.io.Closeable
import java.net.URL
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

// OBFUSCATION: This class has an explicit proguard rule to keep it. See proguard-rules.pro

/**
 * Options for the [ApiVideoPlayerAnalyticsAgent].
 * @param collectorUrl The URL of the api.video player analytics collector.
 * @param batchReportIntervalInMs The interval in milliseconds at which the agent will report events.
 */
private open class ApiVideoPlayerAnalyticsOptions(
    val collectorUrl: String = DEFAULT_URL,
    val collectorPath: String,
    val batchReportIntervalInMs: Long = DEFAULT_BATCH_REPORT_INTERVAL_MS
) {
    companion object {
        private const val DEFAULT_URL = "https://collector.api.video"
        private const val DEFAULT_BATCH_REPORT_INTERVAL_MS = 5000L
    }
}

/**
 * An agent that sends events to the api.video player analytics.
 * @param options The options for the agent.
 * @param sessionId The session ID for the agent.
 * @param version The version of the agent.
 * @param userAgent The user agent for the agent.
 */
class ApiVideoPlayerAnalyticsAgent private constructor(
    private val options: ApiVideoPlayerAnalyticsOptions,
    private val sessionId: String,
    private val version: String,
    private val userAgent: String?
) : Closeable {
    @OptIn(ExperimentalSerializationApi::class)
    private val client = HttpClient(Android) {
        install(ContentNegotiation) {
            json(Json {
                explicitNulls = false
                encodeDefaults = true
            })
        }
        userAgent?.let {
            install(UserAgent) {
                agent = it
            }
        }
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    private var batchGenerator: BatchGenerator? = null

    /**
     * Sets a new media ID for the agent.
     * The media ID is the video ID or the live stream ID from api.video.
     *
     * When the media ID is set, the agent will send events of the previous media ID and start
     * sending events of the new media ID.
     *
     * @param mediaId The media ID to set.
     */
    fun setMediaId(mediaId: String) {
        synchronized(this) {
            batchGenerator?.close()
            batchGenerator = BatchGenerator(
                mediaId, UUID.create(), options.batchReportIntervalInMs.milliseconds, scope
            ) { batch ->
                scope.launch { reportBatch(batch) }
            }
        }
    }

    /**
     * Disables the agent.
     *
     * It will send the events of the previous media ID, then it will stop sending events till
     * [setMediaId] is called again.
     *
     * When the agent is disabled, calling [addEvent] will return false.
     */
    fun disable() {
        synchronized(this) {
            batchGenerator?.close()
            batchGenerator = null
        }
    }

    /**
     * Adds an event to the agent.
     */
    fun addEvent(event: Event): Boolean {
        return synchronized(this) {
            batchGenerator?.addEvent(event) ?: false
        }
    }

    private suspend fun reportBatch(batch: Batch) {
        val httpResponse = client.post(options.collectorUrl) {
            url {
                appendPathSegments(options.collectorPath)
            }
            contentType(ContentType.Application.Json)
            setBody(
                batch
            )
        }

        if (httpResponse.status.value in 200..299) {
            Log.d(TAG, "Successfully reported events $batch")
        } else {
            Log.e(
                TAG,
                "Error: ${httpResponse.status.value}: ${httpResponse.status.description} for payload: $batch"
            )
        }
    }

    override fun close() {
        batchGenerator?.close()
        client.close()
        scope.coroutineContext.cancelChildren()
    }

    companion object {
        private const val TAG = "Agent"

        private const val VERSION = "3.0.0"
        private const val EVENTS_QUEUE_SIZE = 20

        private const val PLAYER_ANALYTICS_COLLECTOR_PATH = "watch"

        /**
         * Creates a new agent.
         *
         * @param collectorUrl The URL of the api.video player analytics collector. Expected format: "https://collector.mycustomdomain.com".
         * @return The agent.
         */
        fun create(collectorUrl: String? = null): ApiVideoPlayerAnalyticsAgent {
            val sessionId = SessionStorage.sessionId
            val options = if (collectorUrl != null) {
                val url = URL(collectorUrl)
                ApiVideoPlayerAnalyticsOptions(
                    "${url.protocol}://${url.host!!}",
                    collectorPath = PLAYER_ANALYTICS_COLLECTOR_PATH
                )
            } else {
                ApiVideoPlayerAnalyticsOptions(collectorPath = PLAYER_ANALYTICS_COLLECTOR_PATH)
            }
            return ApiVideoPlayerAnalyticsAgent(
                options, sessionId, VERSION, PlayerAnalyticsUserAgent.create()
            )
        }
    }

    private inner class BatchGenerator(
        val mediaId: String,
        val playbackId: String,
        batchReportIntervalInMs: Duration,
        scope: CoroutineScope,
        val onNewBatch: (Batch) -> Unit
    ) {
        private val events = FixedSizeListWithUpsert<Event>(EVENTS_QUEUE_SIZE)

        private val tickerFlow = tickerFlow(
            batchReportIntervalInMs, batchReportIntervalInMs
        ).onEach {
            trySendBatch()
        }.launchIn(scope)

        private var hasFirstPlay = false

        private val batch: Batch?
            get() {
                val eventsToReport = synchronized(this) {
                    if (events.isEmpty()) return null

                    val events = this.events.toList()
                    this.events.clear()
                    events
                }

                return Batch(
                    sendAtInMs = System.currentTimeMillis(),
                    sessionId = sessionId,
                    playbackId = playbackId,
                    mediaId = mediaId,
                    events = eventsToReport,
                    version = VERSION,
                    referrer = ""
                )
            }

        private fun trySendBatch() {
            batch?.let {
                onNewBatch(it)
            }
        }

        fun addEvent(event: Event): Boolean {
            val result = synchronized(this) {
                events.add(event)
            }
            if ((event.type == Event.EventType.PLAY) && (!hasFirstPlay)) {
                hasFirstPlay = true
                trySendBatch()
            }
            return result
        }

        fun close() {
            runBlocking {
                tickerFlow.cancelAndJoin()
                trySendBatch()
            }
        }
    }
}

private object SessionStorage {
    val sessionId = UUID.create()
}