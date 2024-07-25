package video.api.player.analytics.core.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a batch of events.
 *
 * @param sendAtInMs The time at which the batch was sent, in milliseconds.
 * @param sessionId The session ID in UUID format.
 * @param playbackId The playback ID in UUID format. It is generated at each new playback. It is not renewed when replaying (same page, same player).
 * @param mediaId The media ID. Either a video Id or a live stream Id.
 * @param events The events in the batch.
 * @param version The version of this agent.
 * @param referrer The referrer of the page where the player is embedded. Always empty for mobile apps.
 */
@Serializable
internal data class Batch(
    @SerialName("sat")
    val sendAtInMs: Long,
    @SerialName("sid")
    val sessionId: String,
    @SerialName("pid")
    val playbackId: String,
    @SerialName("mid")
    val mediaId: String,
    @SerialName("eve")
    val events: List<Event>,
    @SerialName("ver")
    val version: String,
    @SerialName("ref")
    val referrer: String
) {
    init {
        require(sendAtInMs > 0) { "sat must be greater than 0" }
        require(events.isNotEmpty()) { "eve must not be empty" }
        require(events.size <= 20) { "eve must not contain more than 20 elements" }
    }
}