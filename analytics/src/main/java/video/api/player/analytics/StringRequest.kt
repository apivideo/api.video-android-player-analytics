package video.api.player.analytics

import com.android.volley.Response
import com.android.volley.toolbox.StringRequest

class StringRequest(
    method: Int,
    url: String,
    private val body: String?,
    listener: Response.Listener<String>,
    errorListener: Response.ErrorListener?
) : StringRequest(method, url, listener, errorListener) {
    override fun getBody(): ByteArray? {
        return body?.encodeToByteArray()
    }
}