@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.wallpaper

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.sosauce.cuteconnect.R

@Composable
fun AddWallpaperItem(onAddNewWallpaper: (Uri) -> Unit) {


    val context = LocalContext.current
    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            )
            onAddNewWallpaper(uri)
        }
    }


    FilledIconButton(
        onClick = { imagePicker.launch(arrayOf("image/*")) },
        shapes = IconButtonDefaults.shapes(),
        modifier = Modifier.size(IconButtonDefaults.smallContainerSize(
            IconButtonDefaults.IconButtonWidthOption.Wide))
    ) {
        Icon(
            painter = painterResource(R.drawable.close),
            contentDescription = null
        )
    }
}