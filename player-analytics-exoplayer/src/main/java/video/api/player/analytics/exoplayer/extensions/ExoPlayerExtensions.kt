package video.api.player.analytics.exoplayer.extensions

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import video.api.player.analytics.exoplayer.ApiVideoExoPlayerAnalyticsListener

// OBFUSCATION: This class has an explicit proguard rule to keep it. See proguard-rules.pro

/**
 * Adds an [ApiVideoExoPlayerAnalyticsListener] to listen to [ExoPlayer] events.
 *
 * @param collectorUrl The URL of the api.video analytics collector.
 * @return The [AnalyticsListener] that was added to the player.
 */
fun ExoPlayer.addApiVideoAnalyticsListener(collectorUrl: String? = null): AnalyticsListener {
    val agent = ApiVideoExoPlayerAnalyticsListener(this, collectorUrl)
    this.addAnalyticsListener(agent)
    return agent
}