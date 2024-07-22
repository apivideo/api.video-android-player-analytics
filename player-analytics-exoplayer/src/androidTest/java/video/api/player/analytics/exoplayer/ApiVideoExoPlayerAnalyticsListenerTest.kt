package video.api.player.analytics.exoplayer

import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.test.core.app.ApplicationProvider
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import video.api.player.analytics.exoplayer.extensions.addApiVideoAnalyticsListener
import video.api.player.analytics.exoplayer.extensions.currentPositionInS
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * A test to verify if events are sent to the api.video analytics when playing a video with ExoPlayer.
 * The verification must be done by checking the events sent to the api.video analytics.
 */
class ApiVideoExoPlayerAnalyticsListenerTest {

    /**
     * Play a video with ExoPlayer and wait for the player to be released.
     * The expected event would be:
     * - SRC
     * - LOADED
     * - PLAY
     * - multiple TIME_UPDATE
     * - END
     * It will fail if [AnalyticsListener.onPlayerReleased] is not called after [TIMEOUT_S] seconds.
     */
    @Test
    fun playTillEnd() {
        var playbackException: PlaybackException? = null

        val mediaId = InstrumentationRegistry.getArguments()
            .getString("INTEGRATION_MEDIA_ID", DEFAULT_MEDIA_ID)
        Log.e(TAG, "playTillEnd: mediaId: $mediaId")

        val timeoutInS = InstrumentationRegistry.getArguments()
            .getString("INTEGRATION_PLAYBACK_TIMEOUT_S", TIMEOUT_S.toString()).toInt()
        Log.e(TAG, "playTillEnd: timeout: $timeoutInS")

        val exoPlayer = createPlayer(inferManifestUrl(mediaId))
        val countDownLatch = CountDownLatch(1)
        val playerListener = object : AnalyticsListener {
            override fun onPlayerError(
                eventTime: AnalyticsListener.EventTime,
                error: PlaybackException
            ) {
                Log.e(TAG, "onPlayerError: error: $error", error)
                playbackException = error
                countDownLatch.countDown()
            }

            override fun onIsPlayingChanged(
                eventTime: AnalyticsListener.EventTime,
                isPlaying: Boolean
            ) {
                Log.e(TAG, "onIsPlayingChanged: isPlaying: $isPlaying")
            }

            override fun onPlaybackStateChanged(
                eventTime: AnalyticsListener.EventTime,
                state: Int
            ) {
                when (state) {
                    Player.STATE_ENDED -> {
                        /**
                         * Sometimes, the player can be in the ENDED state without having played any media.
                         */
                        if (exoPlayer.currentPositionInS > 0.0f) {
                            Log.e(TAG, "onPlaybackStateChanged: END")
                            exoPlayer.release()
                        } else {
                            return
                        }
                    }

                    else -> return
                }
            }

            override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
                Log.e(TAG, "onPlayerReleased")
                countDownLatch.countDown()
            }
        }
        exoPlayer.addAnalyticsListener(playerListener)

        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            exoPlayer.play()
        }

        // Wait for player to be released
        countDownLatch.await(TIMEOUT_S, TimeUnit.SECONDS)

        assertEquals(0, countDownLatch.count)
        assertNull(playbackException)
    }

    private fun createPlayer(videoUri: String): ExoPlayer {
        val player = ExoPlayer.Builder(ApplicationProvider.getApplicationContext()).build()
        player.addApiVideoAnalyticsListener()
        InstrumentationRegistry.getInstrumentation().runOnMainSync {
            player.prepare()
            player.setMediaItem(MediaItem.fromUri(videoUri))
        }
        return player
    }

    companion object {
        /**
         * The default mediaId to use for the tests
         * /!\ Used in CI, make sure the mediaId is in sync with the the CI.
         * Only for public videos.
         */
        private const val DEFAULT_MEDIA_ID = "vi77Dgk0F8eLwaFOtC5870yn"
        private const val TIMEOUT_S = 60L // 1 min

        private const val TAG = "ExoPlayerListenerTest"

        /**
         * Infers the video URL from the mediaId
         */
        fun inferManifestUrl(mediaId: String): String {
            return when {
                mediaId.startsWith("vi") -> "https://vod.api.video/vod/$mediaId/hls/manifest.m3u8"
                mediaId.startsWith("li") -> "https://live.api.video/$mediaId.m3u8"
                else -> throw IllegalArgumentException("Invalid mediaId: $mediaId")
            }
        }
    }
}