@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.shared_components.CuteDropdownMenuItem
import androidx.compose.material3.Text
@Composable
fun ActionPicker(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onUpdateMediasToSend: (Uri) -> Unit,
) {

    val photoPickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) {
        it?.let { uri ->
            onUpdateMediasToSend(uri)
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        it?.let { uri ->
            onUpdateMediasToSend(uri)
        }
    }


    val actionPickerItems = listOf(
        ActionPickerItem(
            text = R.string.capture,
            icon = R.drawable.camera,
            onClick = {}
        ),
        ActionPickerItem(
            text = R.string.attach_media,
            icon = R.drawable.add_photo,
            onClick = {
                photoPickerLauncher.launch(
                    PickVisualMediaRequest(
                        ActivityResultContracts.PickVisualMedia.ImageAndVideo
                    )
                )
            }
        ),
        ActionPickerItem(
            text = R.string.attach_file,
            icon = R.drawable.file,
            onClick = {
                filePickerLauncher.launch(arrayOf("*/*"))
            }
        )
    )

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismissRequest,
        shape = RoundedCornerShape(24.dp)
    ) {
        actionPickerItems.forEachIndexed { index, item ->
            CuteDropdownMenuItem(
                onClick = {
                    item.onClick()
                    onDismissRequest()
                },
                text = { Text(stringResource(item.text)) },
                leadingIcon = {
                    Icon(
                        painter = painterResource(item.icon),
                        contentDescription = null
                    )
                }
            )
        }
    }
}

private data class ActionPickerItem(
    val text: Int,
    val icon: Int,
    val onClick: () -> Unit
)