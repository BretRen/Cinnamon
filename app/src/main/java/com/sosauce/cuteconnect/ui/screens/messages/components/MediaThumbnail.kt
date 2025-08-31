package com.sosauce.cuteconnect.ui.screens.messages.components

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil3.ImageLoader
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.transform.RoundedCornersTransformation
import coil3.video.VideoFrameDecoder
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.utils.ImageUtils

@Composable
fun MediaThumbnail(
    modifier: Modifier = Modifier,
    media: Uri,
    mediaType: String,
    onRemoveFromList: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = remember { mediaType.startsWith("video/") == true }
    val videoLoader = remember {
        ImageLoader.Builder(context)
            .components {
                if (isVideo) {
                    add(VideoFrameDecoder.Factory())
                }
            }
            .build()
    }
    Box(
        modifier = modifier
            .padding(5.dp)
            .size(100.dp)
            .aspectRatio(1f)
    ) {
        AsyncImage(
            model = ImageUtils.imageRequester(media, context, true),
            contentDescription = null,
            imageLoader = videoLoader,
            contentScale = ContentScale.Crop
        )
        if (isVideo) {
            Icon(
                imageVector = Icons.Rounded.PlayArrow,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
            )

        }
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable { onRemoveFromList() },
        )
    }
}