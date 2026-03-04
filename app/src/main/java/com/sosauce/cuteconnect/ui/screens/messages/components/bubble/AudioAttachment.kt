@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components.bubble

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Handler
import android.provider.Telephony
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.SliderColors
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.buttons.PlayPauseButton
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.ui.shared_components.AnimatedSlider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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