package video.api.player.analytics.example.videoview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.preference.PreferenceManager
import video.api.player.analytics.example.videoview.R
import video.api.player.analytics.example.videoview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val sharedPref by lazy {
        PreferenceManager.getDefaultSharedPreferences(this)
    }

    private val videoUrl: String?
        get() = sharedPref.getString(getString(R.string.video_url_key), null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

        binding.loadButton.setOnClickListener {
            binding.videoView.setVideoURI(videoUrl!!.toUri())
        }
    }

    override fun onPause() {
        super.onPause()
        binding.videoView.suspend()
    }
}