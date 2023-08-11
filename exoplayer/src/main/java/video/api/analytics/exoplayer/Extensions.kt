package video.api.analytics.exoplayer

import androidx.media3.exoplayer.analytics.AnalyticsListener

/**
 * Convert a Long in milliseconds to a Float in seconds.
 */
fun Long.toSeconds() = this.toFloat() / 1000

/**
 * An [AnalyticsListener.EventTime] to a player analytics intelligible value
 */
@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
fun AnalyticsListener.EventTime.toSeconds() = this.currentPlaybackPositionMs.toSeconds()