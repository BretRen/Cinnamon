@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.shared_components

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.utils.ImageUtils

@Composable
fun DefaultContactIcon(
    modifier: Modifier = Modifier,
    firstLetter: Char?,
    size: Dp = 42.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    contactPfp: Uri = Uri.EMPTY,
    shape: Shape = CircleShape
) {

    val context = LocalContext.current
    val density = LocalDensity.current

    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(
                color = color,
                shape = shape
            ),
        contentAlignment = Alignment.Center
    ) {
        // async image will just overlap if it exists, prevents us from using subcomposable async image, which is slower
        if (firstLetter?.isLetter() == true) {
            Text(
                text = firstLetter.uppercase(),
                style = MaterialTheme.typography.titleLargeEmphasized.copy(
                    color = MaterialTheme.colorScheme.contentColorFor(color),
                    fontSize = with(density) { (size / 2).toSp() },
                )
            )
        } else {

            Icon(
                painter = painterResource(R.drawable.person_filled),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.contentColorFor(color),
                modifier = Modifier.size(size / 2)
            )
        }
        AsyncImage(
            model = contactPfp,
            contentDescription = null,
            modifier = Modifier.clip(shape)
        )
    }


}