package video.api.player.analytics.core.utils

import java.util.UUID

internal object UUID {
    fun create() = UUID.randomUUID().toString()
}