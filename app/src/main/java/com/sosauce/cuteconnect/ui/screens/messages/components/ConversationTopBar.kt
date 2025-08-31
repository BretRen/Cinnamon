@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.actions.CallAction
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.shared_components.CuteDropdownMenuItem
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.ui.shared_components.DropdownItemDelete
import com.sosauce.cuteconnect.utils.betterFormatNumber
import com.sosauce.cuteconnect.utils.getContactId
import com.sosauce.cuteconnect.utils.getContactNameOrNothing

@Composable
fun ConversationTopBar(
    modifier: Modifier = Modifier,
    cuteConversation: CuteConversation,
    threadId: Long,
    onNavigateUp: () -> Unit,
    onHandleCallAction: (CallAction) -> Unit,
    onNavigate: (Screen) -> Unit,
    onDeleteConversation: () -> Unit
) {

    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }
    val nameOrNumber = remember { cuteConversation.recipients.first().getContactNameOrNothing(context) }


    HorizontalFloatingToolbar(
        modifier = modifier
            .padding(horizontal = 5.dp)
            .statusBarsPadding()
            .clickable {
                onNavigate(Screen.ContactDetails(cuteConversation.recipients.first().getContactId(context)))
            },
        expanded = false,
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onNavigateUp
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
            DefaultContactIcon(
                firstLetter = nameOrNumber.firstOrNull(),
                modifier = Modifier.padding(end = 10.dp),
                size = 38.dp,
                fontSize = 18.sp
            )
            CuteText(
                text = nameOrNumber.betterFormatNumber(),
                maxLines = 1,
                modifier = Modifier
                    .weight(1f),
                overflow = TextOverflow.Ellipsis
            )

            if (!cuteConversation.isGroupChat) {
                IconButton(
                    onClick = {
                        onHandleCallAction(CallAction.LaunchCall(cuteConversation.recipients.first()))
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null
                    )
                }
            }
            IconButton(
                onClick = { showMoreMenu = true }
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null
                )
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    shape = RoundedCornerShape(24.dp)
                ) {
                    CuteDropdownMenuItem(
                        onClick = { onNavigate(Screen.ConversationTheming(threadId)) },
                        text = { CuteText("Customize chat") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null
                            )
                        }
                    )

                    DropdownItemDelete(
                        onDelete = onDeleteConversation,
                        dialogTitle = { CuteText(stringResource(R.string.delete_convo)) },
                        dialogText = { CuteText(stringResource(R.string.delete_convo_u_sure)) }
                    )
                }
            }

        }
    }
}