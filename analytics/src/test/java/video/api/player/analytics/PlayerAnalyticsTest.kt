package video.api.player.analytics

import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HttpResponse
import com.android.volley.toolbox.NoCache
import com.android.volley.toolbox.Volley
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import org.junit.Assert.*
import org.junit.Test
import video.api.player.analytics.mock.ImmediateResponseDelivery
import video.api.player.analytics.mock.MockHttpStack
import java.io.IOException


const val FAKE_SESSION_ID = "1234"

class PlayerAnalyticsTest {
    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json { explicitNulls = false }

    private fun decodePingMessage(response: ByteArray): PlaybackPingMessage {
        return json.decodeFromString(PlaybackPingMessage.serializer(), response.decodeToString())
    }

    @Test
    fun `session id is properly handled when no session id in cache`() {
        val builder = PlayerAnalyticsTestBuilder()
        val playerAnalytics =
            builder.setResponseSessionId(FAKE_SESSION_ID).build()

        playerAnalytics.play().get()
        assertNull(playerAnalytics.sessionId)

        playerAnalytics.currentTime = 10F
        playerAnalytics.pause().get()
        var pingMessage = builder.mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertNull(pingMessage!!.session.sessionId)

        assertEquals(FAKE_SESSION_ID, playerAnalytics.sessionId)

        playerAnalytics.pause().get()
        pingMessage = builder.mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(FAKE_SESSION_ID, pingMessage!!.session.sessionId)
    }

    @Test
    fun `seek forward`() {
        val builder = PlayerAnalyticsTestBuilder()
        val playerAnalytics =
            builder.setResponseSessionId(FAKE_SESSION_ID).build()
        val from = 10.3F
        val to = 15.3F

        playerAnalytics.seek(from, to)
        playerAnalytics.pause().get()

        val pingMessage = builder.mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(2, pingMessage!!.events.size)
        assertEquals(Event.SEEK_FORWARD, pingMessage.events[0].type)
        assertEquals(from, pingMessage.events[0].from)
        assertEquals(to, pingMessage.events[0].to)

        assertEquals(Event.PAUSE, pingMessage.events[1].type)
    }

    @Test
    fun `seek backward`() {
        val builder = PlayerAnalyticsTestBuilder()
        val playerAnalytics =
            builder.setResponseSessionId(FAKE_SESSION_ID).build()
        val from = 15.3F
        val to = 10.3F

        playerAnalytics.seek(from, to)
        playerAnalytics.pause().get()

        val pingMessage = builder.mockHttpStack.lastPostBody?.let { decodePingMessage(it) }
        assertNotNull(pingMessage)
        assertEquals(2, pingMessage!!.events.size)
        assertEquals(Event.SEEK_BACKWARD, pingMessage.events[0].type)
        assertEquals(from, pingMessage.events[0].from)
        assertEquals(to, pingMessage.events[0].to)

        assertEquals(Event.PAUSE, pingMessage.events[1].type)
    }

    @Test
    fun `send ping failed`() {
        val playerAnalytics =
            PlayerAnalyticsTestBuilder().setResponse(IOException()).build()

        try {
            playerAnalytics.pause().get()
            fail("An exception is expected here")
        } catch (e: Exception) {
        }
    }

    @Test
    fun `receive bad ping response`() {
        val playerAnalytics =
            PlayerAnalyticsTestBuilder().setResponse("odd response").build()

        try {
            playerAnalytics.pause().get()
            fail("An exception is expected here")
        } catch (e: Exception) {
        }
    }
}

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
        return this
    }

    fun setResponse(response: String): PlayerAnalyticsTestBuilder {
        this.response = response
        return this
    }

    fun setResponseSessionId(sessionId: String): PlayerAnalyticsTestBuilder {
        this.response = """{"session" : "$sessionId"}"""
        return this
    }

    fun build(): PlayerAnalytics {
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
            RequestQueue(NoCache(), BasicNetwork(mockHttpStack), 2, ImmediateResponseDelivery())

        mockkStatic(Volley::class)
        every { Volley.newRequestQueue(any()) } returns queue

        return PlayerAnalytics(mockk(relaxed = true), options)
    }
}
