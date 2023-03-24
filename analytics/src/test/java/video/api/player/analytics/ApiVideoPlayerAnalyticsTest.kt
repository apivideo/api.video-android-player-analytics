package video.api.player.analytics

import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HttpResponse
import com.android.volley.toolbox.NoCache
import io.mockk.every
import io.mockk.mockkConstructor
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import video.api.player.analytics.mock.ImmediateResponseDelivery
import video.api.player.analytics.mock.MockHttpStack
import java.io.IOException


const val FAKE_SESSION_ID = "1234"

class ApiVideoPlayerAnalyticsTest {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { explicitNulls = false }

    companion object {
        private val mockHttpStack = MockHttpStack()
    }

    private var defaultOptions = Options(
        VideoInfo(
            videoId = "videoId",
            videoType = VideoType.VOD
        ), emptyMap()
    )

    private fun decodePingMessage(response: ByteArray): PlaybackPingMessage {
        return json.decodeFromString(PlaybackPingMessage.serializer(), response.decodeToString())
    }

    @Before
    fun setUp() {
        // Mock RequestQueue
        val queue =
            RequestQueue(
                NoCache(),
                BasicNetwork(mockHttpStack),
                2,
                ImmediateResponseDelivery()
            ).apply {
                start()
            }
        mockkConstructor(RequestQueue::class)
        every { anyConstructed<RequestQueue>().add(any<Request<Any>>()) } answers {
            queue.add(
                firstArg()
            )
        }
    }

    @Test
    fun `session id is properly handled when no session id in cache`() {
        mockHttpStack.setResponseSessionId(FAKE_SESSION_ID)
        val playerAnalytics = ApiVideoPlayerAnalytics(defaultOptions)
        playerAnalytics.play().get()
        assertNull(playerAnalytics.sessionId)

        playerAnalytics.currentTime = 10F
        playerAnalytics.pause().get()
        var pingMessage = mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertNull(pingMessage!!.session.sessionId)

        assertEquals(FAKE_SESSION_ID, playerAnalytics.sessionId)

        playerAnalytics.pause().get()
        pingMessage = mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(FAKE_SESSION_ID, pingMessage!!.session.sessionId)
    }

    @Test
    fun `seek forward`() {
        val playerAnalytics = ApiVideoPlayerAnalytics(defaultOptions)
        mockHttpStack.setResponseSessionId(FAKE_SESSION_ID)

        val from = 10.3F
        val to = 15.3F

        playerAnalytics.seek(from, to)
        playerAnalytics.pause().get()

        val pingMessage = mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(2, pingMessage!!.events.size)
        assertEquals(Event.SEEK_FORWARD, pingMessage.events[0].type)
        assertEquals(from, pingMessage.events[0].from)
        assertEquals(to, pingMessage.events[0].to)

        assertEquals(Event.PAUSE, pingMessage.events[1].type)
    }

    @Test
    fun `seek backward`() {
        val playerAnalytics = ApiVideoPlayerAnalytics(defaultOptions)
        mockHttpStack.setResponseSessionId(FAKE_SESSION_ID)

        val from = 15.3F
        val to = 10.3F

        playerAnalytics.seek(from, to)
        playerAnalytics.pause().get()

        val pingMessage = mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(2, pingMessage!!.events.size)
        assertEquals(Event.SEEK_BACKWARD, pingMessage.events[0].type)
        assertEquals(from, pingMessage.events[0].from)
        assertEquals(to, pingMessage.events[0].to)

        assertEquals(Event.PAUSE, pingMessage.events[1].type)
    }

    @Test
    fun `set event time test`() {
        val playerAnalytics = ApiVideoPlayerAnalytics(defaultOptions)
        mockHttpStack.setResponseSessionId(FAKE_SESSION_ID)

        val ts = 15.3F

        playerAnalytics.pause(ts).get()

        val pingMessage = mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(1, pingMessage!!.events.size)
        assertEquals(Event.PAUSE, pingMessage.events[0].type)
        assertEquals(ts, pingMessage.events[0].at)
    }

    @Test
    fun `set current time test`() {
        val playerAnalytics = ApiVideoPlayerAnalytics(defaultOptions)
        mockHttpStack.setResponseSessionId(FAKE_SESSION_ID)

        val ts = 5.92F
        playerAnalytics.currentTime = ts
        playerAnalytics.pause().get()

        val pingMessage = mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(1, pingMessage!!.events.size)
        assertEquals(Event.PAUSE, pingMessage.events[0].type)
        assertEquals(ts, pingMessage.events[0].at)
    }

    @Test
    fun `send ping failed`() {
        val playerAnalytics = ApiVideoPlayerAnalytics(defaultOptions)
        mockHttpStack.setExceptionToThrow(IOException())

        try {
            playerAnalytics.pause().get()
            fail("An exception is expected here")
        } catch (_: Exception) {
        }
    }

    @Test
    fun `receive bad ping response`() {
        val playerAnalytics = ApiVideoPlayerAnalytics(defaultOptions)
        mockHttpStack.setResponse("odd response")

        try {
            playerAnalytics.pause().get()
            fail("An exception is expected here")
        } catch (_: Exception) {
        }
    }
}

fun MockHttpStack.setResponseSessionId(sessionId: String) {
    setResponse("""{"session" : "$sessionId"}""")
}

fun MockHttpStack.setResponse(response: String) {
    setResponseToReturn(
        HttpResponse(
            200,
            emptyList(),
            response.toByteArray()
        )
    )
}
/*
class PlayerAnalyticsTestBuilder {
    private var options = Options(
        VideoInfo(
            pingUrl = "https://aa",
            videoId = "videoId",
            videoType = VideoType.VOD
        ), emptyMap()
    )
    private var exception: Exception? = null
    private var response: String? = null
    val mockHttpStack = MockHttpStack()

    fun setOptions(options: Options): PlayerAnalyticsTestBuilder {
        this.options = options
        return this
    }

    fun setResponse(exception: Exception): PlayerAnalyticsTestBuilder {
        this.exception = exception
        this.response = null
        return this
    }

    fun setResponse(response: String): PlayerAnalyticsTestBuilder {
        this.response = response
        this.exception = null
        return this
    }

    fun setResponseSessionId(sessionId: String): PlayerAnalyticsTestBuilder {
        this.response = """{"session" : "$sessionId"}"""
        this.exception = null
        return this
    }

    fun build(): ApiVideoPlayerAnalytics {
        response?.let {
            mockHttpStack.setResponseToReturn(
                HttpResponse(
                    200,
                    emptyList(),
                    it.toByteArray()
                )
            )
        }
        exception?.let {
            mockHttpStack.setExceptionToThrow(it)
        }

        // Mock RequestQueue
        val queue =
            RequestQueue(
                NoCache(),
                BasicNetwork(mockHttpStack),
                2,
                ImmediateResponseDelivery()
            ).apply {
                start()
            }
        mockkConstructor(RequestQueue::class)
        every { anyConstructed<RequestQueue>().add(any<Request<Any>>()) } answers {
            queue.add(
                firstArg()
            )
        }

        return ApiVideoPlayerAnalytics(options)
    }
}*/

object TestRequestQueue {
    val mockHttpStack = MockHttpStack()
}
