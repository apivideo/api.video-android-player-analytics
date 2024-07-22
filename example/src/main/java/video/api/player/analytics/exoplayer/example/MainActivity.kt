package video.api.player.analytics.exoplayer.example

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.preference.PreferenceManager
import video.api.player.analytics.exoplayer.extensions.addApiVideoAnalyticsListener
import video.api.player.analytics.exoplayer.example.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val mediaId: String
        get() = sharedPref.getString(getString(R.string.media_id_key), null)!!
    private val videoUrl: String
        get() = Utils.inferManifestUrl(mediaId)


    private val player by lazy {
        createPlayer()
    }
    private val player2 by lazy {
        createPlayer()
    }

    private fun createPlayer(): ExoPlayer {
        val player = ExoPlayer.Builder(this).build()
        player.addApiVideoAnalyticsListener()
        player.prepare()
        return player
    }

    @androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

        binding.playerView.player = player
        binding.playerView2.player = player2
        binding.loadButton.setOnClickListener {
            try {
                val videoUrl = videoUrl
                Log.i("MainActivity", "Loading video: $videoUrl")
                player.setMediaItem(MediaItem.fromUri(videoUrl))
                player2.setMediaItem(MediaItem.fromUri(videoUrl))
            } catch (e: Exception) {
                Log.e("MainActivity", "Error while loading video", e)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        player.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }
}