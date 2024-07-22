package video.api.player.analytics.core.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import video.api.player.analytics.core.payload.Event.EventType
import video.api.player.analytics.core.utils.EnumAsIntSerializer

// OBFUSCATION: This class has an explicit proguard rule to keep it. See proguard-rules.pro

/**
 * Represents a player event.
 *
 * @param emittedAtInMs The time at which the event was emitted, in milliseconds.
 * @param type The type of the event.
 * @param videoTimeInS The time in the video at which the event was emitted, in seconds.
 * @param videoWidth The width of the video at the time of the event.
 * @param videoHeight The height of the video at the time of the event.
 * @param paused Whether the video was paused at the time of the event.
 * @param errorCode The error code, if the event is of type [EventType.ERROR].
 */
@Serializable
data class Event(
    @SerialName("eat")
    val emittedAtInMs: Long,
    @SerialName("typ")
    val type: EventType,
    @SerialName("vti")
    val videoTimeInS: Float,
    @SerialName("vwi")
    val videoWidth: Int,
    @SerialName("vhe")
    val videoHeight: Int,
    @SerialName("pau")
    val paused: Boolean,
    @SerialName("eco")
    val errorCode: ErrorCode
) {
    /**
     * Serializer for [EventType].
     */
    private class EventSerializer : EnumAsIntSerializer<EventType>(
        "EventType",
        { it.value },
        { v -> EventType.valueOf(v) }
    )

    /**
     * Represents the type of a player analytics event.
     */
    @Serializable(with = EventSerializer::class)
    enum class EventType(val value: Int) {
        LOADED(1),
        PLAY(2),
        PAUSE(3),
        TIME_UPDATE(4),
        ERROR(5),
        END(6),
        SEEK(7),
        SRC(8);

        companion object {
            /**
             * Returns the [EventType] corresponding to the given value.
             */
            fun valueOf(value: Int) = entries.first { it.value == value }
        }
    }

    /**
     * Whether [type] and [paused] are equal.
     */
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Event) return false
        if (this.type != other.type) return false
        if (this.paused != other.paused) return false
        return true
    }

    override fun hashCode(): Int {
        var result = emittedAtInMs.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + videoTimeInS.hashCode()
        result = 31 * result + videoWidth
        result = 31 * result + videoHeight
        result = 31 * result + paused.hashCode()
        result = 31 * result + errorCode.hashCode()
        return result
    }

    companion object {

        /**
         * Creates an event with the current time.
         */
        fun createNow(
            type: EventType,
            videoTimeInS: Float,
            videoWidth: Int,
            videoHeight: Int,
            paused: Boolean,
            errorCode: ErrorCode
        ) =
            Event(
                System.currentTimeMillis(),
                type,
                videoTimeInS,
                videoWidth,
                videoHeight,
                paused,
                errorCode
            )
    }
}
