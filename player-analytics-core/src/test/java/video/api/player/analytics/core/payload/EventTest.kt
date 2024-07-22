package video.api.player.analytics.core.payload

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class EventTest {
    @Test
    fun `test serialization`() {
        val expected =
            """{"eat":1710147604622,"typ":2,"vti":0.0,"vwi":720,"vhe":1280,"pau":false,"eco":0}"""

        val event = Event(
            1710147604622,
            Event.EventType.PLAY,
            0f,
            720,
            1280,
            false,
            ErrorCode.NONE
        )

        val actual = Json.encodeToString(Event.serializer(), event)
        assertEquals(expected, actual)
    }

    @Test
    fun `test equality`() {
        val event1 = Event(
            1710147604622,
            Event.EventType.PLAY,
            0f,
            720,
            1280,
            false,
            ErrorCode.NONE
        )
        val event2 = Event(
            1710147604623,
            Event.EventType.PLAY,
            1f,
            480,
            640,
            false,
            ErrorCode.ABORT
        )
        assertEquals(event1, event2)

        // Test different types
        val event3 = Event(
            1710147604622,
            Event.EventType.PAUSE,
            0f,
            720,
            1280,
            false,
            ErrorCode.NONE
        )

        assertNotEquals(event1, event3)

        // Test different paused
        val event4 = Event(
            1710147604622,
            Event.EventType.PLAY,
            0f,
            720,
            1280,
            true,
            ErrorCode.NONE
        )

        assertNotEquals(event1, event4)
    }
}