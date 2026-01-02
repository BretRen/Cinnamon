package com.sosauce.cuteconnect.ui.screens.messages.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.R

@Composable
fun GenericThumbnail(
    modifier: Modifier = Modifier,
    mediaType: String,
    onRemoveFromList: () -> Unit
) {
    val mediaToIcon = remember {
        when {
            mediaType.startsWith("audio/") -> R.drawable.music
            else -> R.drawable.file_filled
        }
    }

    Box(
        modifier = modifier
            .padding(5.dp)
            .size(100.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(50f))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Icon(
            painter = painterResource(mediaToIcon),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.Center)
        )
        Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                .clickable { onRemoveFromList() },
        )
    }


}