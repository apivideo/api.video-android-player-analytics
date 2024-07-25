package video.api.player.analytics.core.payload

import kotlinx.serialization.Serializable
import video.api.player.analytics.core.utils.EnumAsIntSerializer

private class ErrorCodeSerializer : EnumAsIntSerializer<ErrorCode>(
    "ErrorCode",
    { it.value },
    { v -> ErrorCode.valueOf(v) }
)

// OBFUSCATION: This class has an explicit proguard rule to keep it. See proguard-rules.pro

/**
 * Represents the error codes.
 */
@Serializable(with = ErrorCodeSerializer::class)
enum class ErrorCode(val value: Int) {
    /**
     * No error.
     */
    NONE(0),

    /**
     * The event was aborted by user.
     */
    ABORT(1),

    /**
     * Some kind of network error occurred which prevented the media from being successfully fetched, despite having previously been available.
     */
    NETWORK(2),

    /**
     * Despite having previously been determined to be usable, an error occurred while trying to decode the media resource, resulting in an error.
     */
    DECODING(3),

    /**
     * The media resource was determined to be unsuitable.
     */
    NO_SUPPORT(4);

    companion object {
        /**
         * Returns the [ErrorCode] corresponding to the given value.
         */
        fun valueOf(value: Int) = entries.first { it.value == value }
    }
}
