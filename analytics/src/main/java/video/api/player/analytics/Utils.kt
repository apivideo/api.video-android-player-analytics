package video.api.player.analytics

import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.io.IOException
import java.net.URL
import java.util.regex.Pattern


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

    /**
     * Parse a media URL such as `https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8` to a [VideoInfo] object.
     * @param mediaUrl the media URL to parse
     * @param collectorDomainURL the collector domain URL
     */
    fun parseMediaUrl(
        mediaUrl: URL,
        collectorDomainURL: URL
    ): VideoInfo {
        val regex = "https://[^/]+/(?>(?<type>vod|live)/)?(?>.*/)?(?<id>(vi|li)[^/^.]*)[/.].*"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(mediaUrl.toString())

        if (matcher.groupCount() < 3) {
            throw IOException("The media url doesn't look like an api.video URL.")
        }

        try {
            matcher.find()
            // Group naming is not supported before Android API 26
            val videoId = matcher.group(2) ?: throw IOException("Failed to get videoId")

            // For live, we might not have a type for now because there isn't any `/live/` in the URL.
            val firstGroup = matcher.group(1)
            val videoType = firstGroup?.toVideoType()
                ?: if (videoId.startsWith("li")) VideoType.LIVE else throw IOException("Failed to get videoType")

            return VideoInfo(
                videoId,
                videoType,
                collectorDomainURL
            )
        } catch (e: Exception) {
            throw IOException("The media url $mediaUrl doesn't look like an api.video URL", e)
        }
    }
}