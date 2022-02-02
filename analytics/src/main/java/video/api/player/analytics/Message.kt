package video.api.player.analytics

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ArraySerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
enum class Event {
    @SerialName("play")
    PLAY,

    @SerialName("resume")
    RESUME,

    @SerialName("ready")
    READY,

    @SerialName("pause")
    PAUSE,

    @SerialName("end")
    END,

    @SerialName("seek.forward")
    SEEK_FORWARD,

    @SerialName("seek.backward")
    SEEK_BACKWARD
}

@Serializable
data class Session(
    @SerialName("session_id")
    val sessionId: String?,
    @SerialName("loaded_at")
    val loadedAt: String,
    @SerialName("video_id")
    val videoId: String? = null,
    @SerialName("live_stream_id")
    val liveStreamId: String? = null,
    val referrer: String,
    @Serializable(with = MapAsArraySerializer::class)
    val metadata: Map<String, String>
) {
    companion object {
        fun buildLiveStreamSession(
            sessionId: String?,
            loadedAt: String,
            videoId: String,
            referrer: String,
            metadata: Map<String, String>
        ) = Session(
            sessionId = sessionId,
            loadedAt = loadedAt,
            liveStreamId = videoId,
            referrer = referrer,
            metadata = metadata
        )

        fun buildVodSession(
            sessionId: String?,
            loadedAt: String,
            videoId: String,
            referrer: String,
            metadata: Map<String, String>
        ) = Session(
            sessionId = sessionId,
            loadedAt = loadedAt,
            videoId = videoId,
            referrer = referrer,
            metadata = metadata
        )
    }
}

@Serializable
data class PlaybackPingMessage(
    @SerialName("emitted_at")
    val emittedAt: String = Utils.nowUtcToIso(),
    val session: Session,
    val events: List<PingEvent>
)

@Serializable
data class PingEvent(
    @SerialName("emitted_at")
    val emittedAt: String = Utils.nowUtcToIso(),
    val type: Event,
    val at: Float? = null,
    val from: Float? = null,
    val to: Float? = null
)

/**
 * Serializes a [Map] as an array of JSON objects which each have a "key" and "value"
 * instead of a single JSON object containing values for all keys in the map, which is the default.
 *
 * This is useful when using JSON serialization and the serialized keys in a map violate JSON recommendations.
 * E.g., the key may include discouraged symbols such as '.', or '$' for BSON.
 */
class MapAsArraySerializer<K, V>(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>
) : KSerializer<Map<K, V>> {
    @Serializable
    data class KeyValue<K, V>(val key: K, val value: V)

    @OptIn(ExperimentalSerializationApi::class)
    private val arraySerializer =
        ArraySerializer(KeyValue.serializer(keySerializer, valueSerializer))
    override val descriptor: SerialDescriptor = arraySerializer.descriptor

    override fun deserialize(decoder: Decoder): Map<K, V> = decoder
        .decodeSerializableValue(arraySerializer)
        .map { it.key to it.value }
        .toMap()

    override fun serialize(encoder: Encoder, value: Map<K, V>) = encoder
        .encodeSerializableValue(
            arraySerializer,
            value.map { KeyValue(it.key, it.value) }.toTypedArray()
        )
}
