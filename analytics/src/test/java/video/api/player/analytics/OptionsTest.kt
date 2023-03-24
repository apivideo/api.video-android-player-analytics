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
                mediaUrl = "https://cdn.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8",
                metadata = emptyMap()
            )

        assertEquals("vi5oDagRVJBSKHxSiPux5rYD", options.videoInfo.videoId)
        assertEquals(VideoType.VOD, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/vod", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid vod media url`() {
        val options =
            Options(
                mediaUrl = "https://vod.api.video/vod/vi5oDagRVJBSKHxSiPux5rYD/hls/manifest.m3u8",
                metadata = emptyMap()
            )

        assertEquals("vi5oDagRVJBSKHxSiPux5rYD", options.videoInfo.videoId)
        assertEquals(VideoType.VOD, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/vod", options.videoInfo.pingUrl)
    }

    @Test
    fun `parse valid live media url`() {
        val options =
            Options(
                mediaUrl = "https://live.api.video/li6Anin2CG1eWirOCBnvYDzI.m3u",
                metadata = emptyMap()
            )

        assertEquals("li6Anin2CG1eWirOCBnvYDzI", options.videoInfo.videoId)
        assertEquals(VideoType.LIVE, options.videoInfo.videoType)
        assertEquals("https://collector.api.video/live", options.videoInfo.pingUrl)
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
}