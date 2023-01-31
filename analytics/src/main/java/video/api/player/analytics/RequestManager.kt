package video.api.player.analytics

import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.NoCache
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

object RequestManager {
    private val queue = RequestQueue(NoCache(), BasicNetwork(HurlStack())).apply { start() }

    @OptIn(ExperimentalSerializationApi::class)
    private val json = Json {
        explicitNulls = false
        encodeDefaults = true
    }

    fun sendPing(
        url: String,
        payload: PlaybackPingMessage,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ): Future<Unit> {
        val future = CompletableFuture<Unit>()
        val stringRequest = StringRequest(
            Request.Method.POST,
            url,
            json.encodeToString(payload),
            { response ->
                try {
                    val jsonResponse = Json.parseToJsonElement(response).jsonObject
                    jsonResponse["session"]?.let {
                        val sessionId = it.jsonPrimitive.content
                        onSuccess(sessionId)
                    } ?: onError(UnsupportedOperationException("No session id in response"))

                } catch (e: Exception) {
                    onError(e)
                }
            },
            { error ->
                onError(error)
            })

        queue.add(stringRequest)
        return future
    }
}