package video.api.player.analytics.example.exoplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
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
            val mediaItem = MediaItem.fromUri(videoUrl)
            player.setMediaSource(DefaultMediaSourceFactory(this).createMediaSource(mediaItem))
            player.addAnalyticsListener(ApiVideoAnalyticsListener(player, videoUrl))
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