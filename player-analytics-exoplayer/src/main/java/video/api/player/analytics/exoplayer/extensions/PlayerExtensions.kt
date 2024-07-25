package video.api.player.analytics.exoplayer.extensions

import androidx.media3.common.C
import androidx.media3.common.Player

/**
 * Gets the current position in second.
 */
internal val Player.currentPositionInS: Float
    get() {
        val currentPosition = currentPosition
        return when {
            currentPosition == C.TIME_UNSET -> 0.0f
            currentPosition < 0 -> 0.0f
            else -> currentPosition.toFloat() / 1000
        }
    }