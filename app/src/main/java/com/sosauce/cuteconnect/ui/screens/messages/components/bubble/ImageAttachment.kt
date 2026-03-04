package com.sosauce.cuteconnect.ui.screens.messages.components.bubble

import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sosauce.cuteconnect.utils.ImageUtils

@Composable
fun ImageAttachment(
    modifier: Modifier = Modifier,
    image: Uri?
) {

    AsyncImage(
        model = image,
        contentDescription = null,
        modifier = modifier.clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Crop
    )
}