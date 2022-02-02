package video.api.player.analytics

import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter


object Utils {
    fun nowUtcToIso(): String {
        val zdt = ZonedDateTime.now(ZoneId.of("UTC"))
        return zdt.format(DateTimeFormatter.ISO_INSTANT)
    }
}