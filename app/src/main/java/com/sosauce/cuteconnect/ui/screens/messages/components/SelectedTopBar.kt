@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components

import android.provider.Telephony
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CopyAll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.actions.CommonAction
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText

@Composable
fun SelectedTopBar(
    selectedCuteMessages: List<CuteMessage>,
    onUnselectAll: () -> Unit,
    onHandleCommonAction: (CommonAction) -> Unit,
) {

    val clipboardManager = LocalClipboardManager.current
    var showDeleteMsgDialog by remember { mutableStateOf(false) }

    if (showDeleteMsgDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteMsgDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        selectedCuteMessages.fastForEach { cuteMessage ->
                            onHandleCommonAction(
                                CommonAction.DeleteFromContentUri(
                                    Telephony.Sms.CONTENT_URI,
                                    cuteMessage.id
                                )
                            )
                        }
                        onUnselectAll()
                        showDeleteMsgDialog = false
                    }
                ) {
                    CuteText(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteMsgDialog = false }
                ) {
                    CuteText(stringResource(R.string.cancel))
                }
            },
            text = {
                CuteText(
                    text = pluralStringResource(R.plurals.delete_msg_u_sure, selectedCuteMessages.size, selectedCuteMessages.size)
                )
            },
            title = {
                CuteText(
                    text = pluralStringResource(R.plurals.delete_msg, selectedCuteMessages.size, selectedCuteMessages.size)
                )
            }
        )
    }
    HorizontalFloatingToolbar(
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .statusBarsPadding(),
        expanded = false,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onUnselectAll
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = null
                )
            }
            CuteText(selectedCuteMessages.size.toString())
            Spacer(Modifier.weight(1f))
            AnimatedVisibility(selectedCuteMessages.size == 1) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(
                            AnnotatedString(selectedCuteMessages.first().body)
                        )
                        onUnselectAll()
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CopyAll,
                        contentDescription = null
                    )
                }
            }
            IconButton(
                onClick = { showDeleteMsgDialog = true }
            ) {
                Icon(
                    painter = painterResource(R.drawable.delete_filled),
                    contentDescription = null
                )
            }
        }
    }
}