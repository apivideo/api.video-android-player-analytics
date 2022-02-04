package video.api.player.analytics

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

/**
 * A volley [StringRequest] with a String body.
 */
class StringRequest(
    method: Int,
    url: String,
    private val body: String?,
    listener: Response.Listener<String>,
    errorListener: Response.ErrorListener?
) : StringRequest(method, url, listener, errorListener) {
    /**
     * Get [String] body field as [ByteArray]
     *
     * @return body as  [ByteArray]
     */
    override fun getBody(): ByteArray? {
        return body?.encodeToByteArray()
    }
}