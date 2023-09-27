package video.api.player.analytics.example.videoview

import android.content.Context
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.widget.MediaController
import android.widget.VideoView
import video.api.player.analytics.Options
import video.api.player.analytics.ApiVideoPlayerAnalytics
import java.util.*
import kotlin.concurrent.timerTask

/**
 * [AnalyticsVideoView] purpose is to override [pause]/[resume]/[start]/[seekTo]/... methods to send
 * analytics.
 */
class AnalyticsVideoView(context: Context, attrs: AttributeSet) :
    VideoView(context, attrs) {
    companion object {
        const val TAG = "VideoView"
    }

    private var playerAnalytics: ApiVideoPlayerAnalytics? = null
    private val timer = Timer().apply {
        scheduleAtFixedRate(timerTask {
            playerAnalytics?.currentTime = currentTime
        }, 0, 33L) // Updates time every 33 ms ~ 30Hz for 30 fps
    }

    private var isFirstPlay = true

    private val currentTime: Float
        get() = currentPosition.toAnalyticsTime()

    init {
        setMediaController(MediaController(context))
        setOnPreparedListener { mediaPlayer ->
            playerAnalytics?.ready()
            this.start() // Autoplay

            mediaPlayer.setOnCompletionListener {
                timer.cancel()
                playerAnalytics?.end()
            }
        }
    }

    override fun start() {
        // As we don't know if video is already running or paused, this is workaround
        if (isFirstPlay) {
            playerAnalytics?.play()
            isFirstPlay = false
        } else {
            playerAnalytics?.resume()
        }
        super.start()
    }

    override fun pause() {
        playerAnalytics?.pause()
        super.pause()
    }

    override fun resume() {
        playerAnalytics?.resume()
        super.resume()
    }

    override fun seekTo(msec: Int) {
        val from = currentTime
        try {
            playerAnalytics?.seek(from, msec.toAnalyticsTime())
        } catch (e: Exception) {
            Log.e(TAG, "Seeking from $from to ${msec.toAnalyticsTime()} failed: $e", e)
        }
        super.seekTo(msec)
    }

    override fun setVideoURI(uri: Uri) {
        super.setVideoURI(uri)
        isFirstPlay = true
        playerAnalytics?.destroy()
        playerAnalytics = ApiVideoPlayerAnalytics(
            Options(
                uri.toString(),
                emptyMap(),
                { sessionId -> Log.i(TAG, "Session ID is $sessionId") },
                { message -> Log.i(TAG, "Send message $message") }
            ))
    }

    override fun suspend() {
        playerAnalytics?.destroy()
        super.suspend()
    }

    /**
     * Converts ms to player analytics time.
     * Example: if the value is 5460ms, analytics time is 5.460s.
     *
     * @return float value in second
     */
    private fun Int.toAnalyticsTime() = this.toFloat() / 1000  // from ms to s
}