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
     * @param vodDomainURL the vod domain URL
     * @param liveDomainURL the live domain URL
     */
    fun parseMediaUrl(
        mediaUrl: URL,
        collectorDomainURL: URL,
        vodDomainURLs: List<URL>,
        liveDomainURL: URL
    ): VideoInfo {
        val regex =
            "https:/.*[/].*/(?<id>(vi|li)[^/^.]*)[/.].*"
        val pattern = Pattern.compile(regex)
        val matcher = pattern.matcher(mediaUrl.toString())

        if (matcher.groupCount() < 2) {
            throw IOException("The media url doesn't look like an api.video URL.")
        }

        try {
            matcher.find()
            // Group naming is not supported before Android API 26
            val videoType =
                if (vodDomainURLs.any { mediaUrl.toString().startsWith(it.toString()) }) {
                    VideoType.VOD
                } else if (mediaUrl.toString().startsWith(liveDomainURL.toString())) {
                    VideoType.LIVE
                } else {
                    throw IOException("The media url must start with ${vodDomainURLs.joinToString(", ")} or $liveDomainURL")
                }
            val videoId = matcher.group(1) ?: throw IOException("Failed to get videoId")

            return VideoInfo(
                videoId,
                videoType,
                collectorDomainURL
            )
        } catch (e: Exception) {
            throw IOException("The media url doesn't look like an api.video URL", e)
        }
    }
}