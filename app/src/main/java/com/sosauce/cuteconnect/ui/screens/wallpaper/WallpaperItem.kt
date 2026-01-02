@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.wallpaper

import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.carousel.CarouselItemScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.utils.ImageUtils

@Composable
fun CarouselItemScope.WallpaperItem(
    wallpaper: String,
    isCurrentWallpaper: Boolean,
    onSetAsWallpaper: () -> Unit,
    onDeleteWallpaper: () -> Unit,
    blurIntensity: Int
) {
    val context = LocalContext.current
    //val animateBlur by animateIntAsState(blurIntensity)
    Box(
        modifier = Modifier
            .height(180.dp)
            .maskClip(MaterialTheme.shapes.extraLarge)
            .clickable {
                if (isCurrentWallpaper) {
                    onDeleteWallpaper()
                } else {
                    onSetAsWallpaper()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = remember { ImageUtils.imageRequester(wallpaper, context) },
            contentDescription = null,
            contentScale = ContentScale.Crop
        )
        androidx.compose.animation.AnimatedVisibility(
            visible = isCurrentWallpaper,
            enter = scaleIn(),
            exit = scaleOut()
        ) {
            Icon(
                painter = painterResource(R.drawable.check),
                contentDescription = null,
                modifier = Modifier
                    .size(IconButtonDefaults.smallContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide))
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}