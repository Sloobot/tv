package com.bitsnbytes.exiontv.iptv

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.bitsnbytes.exiontv.iptv.databinding.ActivityTvPlayerBinding
import com.bitsnbytes.exiontv.iptv.databinding.ChannelLayoutBinding
import com.bitsnbytes.exiontv.iptv.models.Channel
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.hls.HlsMediaSource
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource
import com.google.android.exoplayer2.util.Util

class TvPlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityTvPlayerBinding
    private lateinit var channel: Channel

    private var _player: SimpleExoPlayer? = null
    private val player get() = _player!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTvPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tempChannel = intent.getParcelableExtra<Channel>("CHANNEL")
        if(tempChannel == null) {
            finish()
            return
        }

        channel = tempChannel

        binding.btnClose.setOnClickListener {
            finish()
        }
    }

    private fun initializePlayer() {
        val trackSelector = DefaultTrackSelector(this)
        trackSelector.setParameters(trackSelector.buildUponParameters().setMaxVideoSizeSd())

        _player = SimpleExoPlayer.Builder(this)
            .setTrackSelector(trackSelector)
            .build()

        binding.tvPlayer.player = player
        binding.tvPlayer.findViewById<TextView>(R.id.channelName).text = channel.name

        player.addListener(object : Player.EventListener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_IDLE || playbackState == Player.STATE_ENDED || !playWhenReady) {
                    binding.tvPlayer.keepScreenOn = false
                } else {
                    // This prevents the screen from getting dim/lock
                    binding.tvPlayer.keepScreenOn = true
                    if (playbackState == Player.STATE_BUFFERING) {
                        binding.progressBar.visibility = View.VISIBLE
                    } else if (playbackState == Player.STATE_READY) {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) { }

            override fun onPlayerError(error: ExoPlaybackException) {
                binding.progressBar.visibility = View.GONE
                binding.errorMessage.visibility = View.VISIBLE
            }
        })

        binding.tvPlayer.setControllerVisibilityListener { visibility ->
            binding.btnClose.visibility = visibility
        }

        hideSystemUi()
        binding.tvPlayer.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

        buildMediaSource(Uri.parse(channel.url))
    }

    private fun buildMediaSource(uri: Uri) {
        val contentType = Util.inferContentType(uri)

        // Create a data source factory.
        val dataSourceFactory: DataSource.Factory =
            DefaultHttpDataSource.Factory().setUserAgent(Util.getUserAgent(this, getString(R.string.app_name)))

        val mediaSource: MediaSource = when(contentType) {
            C.TYPE_HLS -> HlsMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
            else -> ProgressiveMediaSource.Factory(dataSourceFactory).createMediaSource(MediaItem.fromUri(uri))
        }

        player.setMediaSource(mediaSource)
        player.prepare()
        player.playWhenReady = true
    }

    private fun releasePlayer() {
        if (_player != null) {
            binding.tvPlayer.player = null
            player.release()
            _player = null
        }
    }

    private fun hideSystemUi() {
        binding.tvPlayer.systemUiVisibility = (View.SYSTEM_UI_FLAG_LOW_PROFILE
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)
    }

    override fun onStart() {
        super.onStart()

        if (Util.SDK_INT >= 24) {
            initializePlayer()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemUi()

        if ((Util.SDK_INT < 24 && _player == null)) {
            initializePlayer()
        }
        else {
            player.playWhenReady = true
        }
    }

    override fun onPause() {
        super.onPause()

        if (Util.SDK_INT < 24) {
            player.playWhenReady = false
        }
    }

    override fun onStop() {
        super.onStop()

        if (Util.SDK_INT >= 24) {
            player.playWhenReady = false
        }
    }

    override fun onDestroy() {
        intent.removeExtra("CHANNEL")
        releasePlayer()
        super.onDestroy()
    }
}