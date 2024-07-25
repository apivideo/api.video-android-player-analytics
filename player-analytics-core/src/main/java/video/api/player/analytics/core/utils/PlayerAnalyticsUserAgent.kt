package video.api.player.analytics.core.utils

internal object PlayerAnalyticsUserAgent {
    fun create(): String? {
        return System.getProperty("http.agent")
    }
}