package com.bitsnbytes.exiontv.iptv

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsets
import android.view.WindowManager
import com.bitsnbytes.exiontv.iptv.databinding.ActivityYoutubePlayerBinding
import com.bitsnbytes.exiontv.iptv.models.Trailer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class YoutubePlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityYoutubePlayerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        else
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN)

        binding = ActivityYoutubePlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trailer = intent.getParcelableExtra<Trailer>("MOVIE_TRAILER")
        if(trailer == null) {
            finish()
            return
        }

        lifecycle.addObserver(binding.ytPlayerView)
        binding.ytPlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(trailer.key, 0f)
                binding.ytPlayerView.enterFullScreen()
            }
        })
    }

    override fun onDestroy() {
        intent.removeExtra("MOVIE_TRAILER")
        super.onDestroy()
    }
}