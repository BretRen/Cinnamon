package com.sosauce.cinnamon.data.managers

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer

object AudioManager {

    private var exoPlayer: ExoPlayer? = null
    var isPlaying by mutableStateOf(false)

    private val listener = object: Player.Listener {
        override fun onIsPlayingChanged(isExoPlaying: Boolean) {
            super.onIsPlayingChanged(isExoPlaying)
            isPlaying = isExoPlaying
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            super.onPlaybackStateChanged(playbackState)

            if (playbackState == ExoPlayer.STATE_ENDED) {
                exoPlayer?.seekToDefaultPosition()
            }
        }
    }


    @SuppressLint("UnsafeOptInUsageError")
    fun initializePlayer(context: Context) {
        exoPlayer = ExoPlayer.Builder(context)
            .setAudioAttributes(
                AudioAttributes.DEFAULT,
                true
            )
            .build()
            .apply {
                prepare()
                addListener(listener)
                pauseAtEndOfMediaItems = true
            }
    }

    fun setMediaItem(uri: Uri) {
        val mediaItem = MediaItem.fromUri(uri)
        val currentItem = if (exoPlayer!!.mediaItemCount > 0) {
            exoPlayer?.getMediaItemAt(0)
        } else MediaItem.EMPTY

        if (currentItem != mediaItem) {
            exoPlayer?.apply { setMediaItem(mediaItem) }
        }
    }


    fun playOrPause() {
        if (exoPlayer?.isPlaying == true) {
            exoPlayer?.pause()
        } else exoPlayer?.play()
    }

    fun releasePlayer() {
        exoPlayer?.removeListener(listener)
        exoPlayer?.release()
    }

}