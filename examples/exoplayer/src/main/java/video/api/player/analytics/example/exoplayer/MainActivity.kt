package video.api.player.analytics.example.exoplayer

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.preference.PreferenceManager
import video.api.analytics.exoplayer.ApiVideoAnalyticsListener
import video.api.player.analytics.example.exoplayer.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val videoUrl: String
        get() = sharedPref.getString(getString(R.string.video_url_key), "")!!

    private val player by lazy {
        val player = ExoPlayer.Builder(this).build()
        player.prepare()
        player
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
        binding.loadButton.setOnClickListener {
            try {
                player.setMediaItem(MediaItem.fromUri(videoUrl))
                player.addAnalyticsListener(ApiVideoAnalyticsListener(player, videoUrl))
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