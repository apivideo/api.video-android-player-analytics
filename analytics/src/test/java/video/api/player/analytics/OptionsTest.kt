package video.api.player.analytics

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test
import java.io.IOException

class OptionsTest {
    @Test
    fun `parse valid former vod media url`() {
        val options =
            Options(
                mediaUrl = "https://cdn.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8"
            )

        assertEquals("vi5oDagRVJBSKHxSiPux5rYD", options.videoInfo.videoId)
        assertEquals(VideoType.VOD, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/vod", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid vod media url`() {
        val options =
            Options(
                mediaUrl = "https://vod.api.video/vod/vi5oNqxkifcXkT4auGNsvgZB/hls/manifest.m3u8"
            )

        assertEquals("vi5oNqxkifcXkT4auGNsvgZB", options.videoInfo.videoId)
        assertEquals(VideoType.VOD, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/vod", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid MP4 vod media url`() {
        val options =
            Options(
                mediaUrl = "https://vod.api.video/vod/vi5oNqxkifcXkT4auGNsvgZB/mp4/source.mp4"
            )

        assertEquals("vi5oNqxkifcXkT4auGNsvgZB", options.videoInfo.videoId)
        assertEquals(VideoType.VOD, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/vod", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid private vod media url`() {
        val options =
            Options(
                mediaUrl = "https://vod.api.video/vod/vi5oNqxkifcXkT4auGNsvgZB/token/PRIVATE_TOKEN/hls/manifest.m3u8"
            )

        assertEquals("vi5oNqxkifcXkT4auGNsvgZB", options.videoInfo.videoId)
        assertEquals(VideoType.VOD, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/vod", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid live media url`() {
        val options =
            Options(
                mediaUrl = "https://live.api.video/li77ACbZjzEJgmr8d0tm4xFt.m3u8"
            )

        assertEquals("li77ACbZjzEJgmr8d0tm4xFt", options.videoInfo.videoId)
        assertEquals(VideoType.LIVE, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/live", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid private live media url`() {
        val options =
            Options(
                mediaUrl = "https://live.api.video/private/PRIVATE_TOKEN/li77ACbZjzEJgmr8d0tm4xFt.m3u8"
            )

        assertEquals("li77ACbZjzEJgmr8d0tm4xFt", options.videoInfo.videoId)
        assertEquals(VideoType.LIVE, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/live", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid vod media url with custom domain`() {
        val videoInfo = VideoInfo.fromMediaURL(
            "https://mycustom.vod.domain/vod/vi5oNqxkifcXkT4auGNsvgZB/hls/manifest.m3u8",
            "https://mycustom.collector.domain",
            "https://mycustom.vod.domain",
            "https://mycustom.live.domain"
        )

        assertEquals("vi5oNqxkifcXkT4auGNsvgZB", videoInfo.videoId)
        assertEquals(VideoType.VOD, videoInfo.videoType)
        assertEquals("https://mycustom.collector.domain/vod", videoInfo.pingUrl)
    }

    @Test
    fun `parse valid private vod media url with custom domain`() {
        val videoInfo = VideoInfo.fromMediaURL(
            "https://mycustom.vod.domain/vod/vi5oNqxkifcXkT4auGNsvgZB/token/PRIVATE_TOKEN/hls/manifest.m3u8",
            "https://mycustom.collector.domain",
            "https://mycustom.vod.domain",
            "https://mycustom.live.domain"
        )

        assertEquals("vi5oNqxkifcXkT4auGNsvgZB", videoInfo.videoId)
        assertEquals(VideoType.VOD, videoInfo.videoType)
        assertEquals("https://mycustom.collector.domain/vod", videoInfo.pingUrl)
    }

    @Test
    fun `parse valid live media url with custom domain`() {
        val videoInfo = VideoInfo.fromMediaURL(
            "https://mycustom.live.domain/li77ACbZjzEJgmr8d0tm4xFt.m3u8",
            "https://mycustom.collector.domain",
            "https://mycustom.vod.domain",
            "https://mycustom.live.domain"
        )

        assertEquals("li77ACbZjzEJgmr8d0tm4xFt", videoInfo.videoId)
        assertEquals(VideoType.LIVE, videoInfo.videoType)
        assertEquals("https://mycustom.collector.domain/live", videoInfo.pingUrl)
    }

    @Test
    fun `parse valid private live media url with custom domain`() {
        val videoInfo = VideoInfo.fromMediaURL(
            "https://mycustom.live.domain/private/PRIVATE_TOKEN/li77ACbZjzEJgmr8d0tm4xFt.m3u8",
            "https://mycustom.collector.domain",
            "https://mycustom.vod.domain",
            "https://mycustom.live.domain"
        )

        assertEquals("li77ACbZjzEJgmr8d0tm4xFt", videoInfo.videoId)
        assertEquals(VideoType.LIVE, videoInfo.videoType)
        assertEquals("https://mycustom.collector.domain/live", videoInfo.pingUrl)
    }

    @Test
    fun `parse invalid media url`() {
        assertThrows(IOException::class.java) {
            Options(
                mediaUrl = "https://mydomain/video.m3u8",
                metadata = emptyMap()
            )
        }
    }

    @Test
    fun `parse invalid media url with custom domain`() {
        assertThrows(IOException::class.java) {
            VideoInfo.fromMediaURL(
                "https://vod.api.video/vod/vi5oNqxkifcXkT4auGNsvgZB/mp4/source.mp4",
                "https://mycustom.collector.domain",
                "https://mycustom.vod.domain",
                "https://mycustom.live.domain"
            )
        }
    }
}