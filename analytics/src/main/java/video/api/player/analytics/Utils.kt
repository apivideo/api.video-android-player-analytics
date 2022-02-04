package video.api.player.analytics

import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter


object Utils {
    /**
     * Get UTC now as String in ISO format.
     * Example:'2011-12-03T10:15:30Z'
     *
     * @return UTC now as a String in ISO format
     */
    fun nowUtcToIso(): String {
        val zdt = ZonedDateTime.now(ZoneId.of("UTC"))
        return zdt.format(DateTimeFormatter.ISO_INSTANT)
    }
}