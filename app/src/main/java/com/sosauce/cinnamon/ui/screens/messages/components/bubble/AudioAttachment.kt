@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.messages.components.bubble

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.buttons.PlayPauseButton
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.ui.shared_components.AnimatedSlider
import kotlinx.coroutines.delay

@SuppressLint("UnsafeOptInUsageError")
@Composable
fun AudioAttachment(
    audio: Uri,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var position by remember { mutableFloatStateOf(0f) }
    var duration by remember { mutableFloatStateOf(0f) }
    val player = remember {
        ExoPlayer.Builder(context.applicationContext).build().apply {
            setMediaItem(MediaItem.fromUri(audio))
            prepare()
        }
    }


    LaunchedEffect(Unit) {
        while (true) {
            position = player.currentPosition.toFloat()
            duration = player.duration.coerceAtLeast(0).toFloat()
            delay(500)
        }
    }

    RetainedEffect(player) {
        onRetire {
            player.release()
        }
    }


    Box(modifier = modifier) {
        Row(
            modifier = Modifier.padding(10.dp)
        ) {
            PlayPauseButton(player) {
                IconButton(
                    onClick = this::onClick,
                    shapes = IconButtonDefaults.shapes()
                ) {
                    val icon = if (this.showPlay) {
                        R.drawable.play_filled
                    } else R.drawable.pause_filled

                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null
                    )
                }
            }

            var tempSlider by remember { mutableStateOf<Float?>(null) }

            AnimatedSlider(
                value = tempSlider ?: position,
                onValueChanged = { tempSlider = it },
                onValueChangeFinished = {
                    player.seekTo(tempSlider!!.toLong())
                    tempSlider = null
                },
                valueRange = 0f..duration,
                modifier = Modifier.weight(1f)
            )
        }
    }

}