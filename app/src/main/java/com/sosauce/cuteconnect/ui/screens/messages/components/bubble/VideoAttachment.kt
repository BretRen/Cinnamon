package com.sosauce.cuteconnect.ui.screens.messages.components.bubble

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.runtime.retain.retain
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.ContentFrame
import androidx.media3.ui.compose.PlayerSurface
import androidx.media3.ui.compose.SURFACE_TYPE_TEXTURE_VIEW
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.video.VideoFrameDecoder
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.utils.ImageUtils

@Composable
fun VideoAttachment(
    video: Uri,
    modifier: Modifier = Modifier
) {


    val context = LocalContext.current
    val request = remember(video) { ImageUtils.videoFrameRequester(video, context) }


    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
    ) {
        AsyncImage(
            model = request,
            contentDescription = null
        )
        IconButton(
            onClick = {},
            colors = IconButtonDefaults.iconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
            ),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                painter = painterResource(R.drawable.play_filled),
                contentDescription = null
            )
        }
        Row(
            modifier = Modifier
                .padding(5.dp)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(24.dp)
                )
                .align(Alignment.BottomStart)

        ) {
            Text(
                text = "00:05",
                modifier = Modifier.padding(3.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }


//
//    val player = remember {
//        ExoPlayer
//            .Builder(context.applicationContext)
//            .build()
//            .apply {
//                addMediaItem(MediaItem.fromUri(video))
//                prepare()
//            }
//    }
//
//    RetainedEffect(player) {
//        onRetire {
//            player.release()
//        }
//    }
//
//    Box(
//        modifier = modifier.size(800.dp)
//    ) {
//        ContentFrame(
//            player = player,
//            modifier = Modifier.fillMaxSize()
//        )
//    }
}