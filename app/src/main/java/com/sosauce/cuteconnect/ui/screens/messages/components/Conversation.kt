@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.compose.ui.util.fastMap
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.ui.shared_components.DefaultGroupChatIcon
import com.sosauce.cuteconnect.utils.betterFormatNumber
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.utils.getContactPfpUri
import com.sosauce.cuteconnect.utils.toDate

@Composable
fun Conversation(
    modifier: Modifier = Modifier,
    cuteConversation: CuteConversation,
    onClick: () -> Unit,
    shape: Shape = RoundedCornerShape(24.dp)
) {
    val context = LocalContext.current
    val nameOrNumber = remember(cuteConversation.recipients) {
        cuteConversation.recipients.fastMap { it.getContactNameOrNothing(context).betterFormatNumber() }
    }
    var showUnblockDialog by remember { mutableStateOf(false) }



    if (showUnblockDialog) {
        AlertDialog(
            onDismissRequest = { showUnblockDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        //BlockedNumberContract.unblock(context, cuteConversation.recipients.first())
                        showUnblockDialog = false
                    }
                ) {
                    Text(
                        text = stringResource(R.string.unblock)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUnblockDialog = false }
                ) {
                    Text(
                        text = stringResource(R.string.cancel)
                    )
                }
            },
            text = {
                Text(
                    text = stringResource(
                        R.string.unblock_no_u_sure,
                        cuteConversation.recipients.first()
                    )
                )
            },
            title = {
                Text(
                    text = stringResource(R.string.unblock_no)
                )
            }
        )
    }



    Surface(
        onClick = onClick,
        shape = shape,
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 1.dp,
                horizontal = 5.dp
            )
    ) {
        Row(
            modifier = modifier
                .padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (cuteConversation.isGroupChat) {
                DefaultGroupChatIcon(
                    modifier = Modifier.padding(start = 10.dp)
                )
            } else {
                DefaultContactIcon(
                    modifier = Modifier.padding(start = 10.dp),
                    firstLetter = nameOrNumber.firstOrNull()?.firstOrNull(),
                    contactPfp = cuteConversation.recipients.first().getContactPfpUri(context)
                )
            }
            Column(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .weight(1f)
            ) {
                Text(
                    text = buildString {
                        nameOrNumber.fastForEachIndexed { index, text ->
                            append(text)
                            if (index != nameOrNumber.lastIndex) {
                                append(", ")
                            }
                        }
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (cuteConversation.isSenderBlocked) {
                        stringResource(R.string.you_blocked_this_no)
                    } else {
//                        if (conversationSettings.draft.isNotEmpty()) {
//                            buildAnnotatedString {
//                                withStyle(SpanStyle(color = MaterialTheme.colorScheme.primary)) { append("Draft:") }
//                                append(" ")
//                                append(conversationSettings.draft)
//                            }.text
//                        } else {
//                        }
                        cuteConversation.snippet
                    },
                    maxLines = if (cuteConversation.read) 1 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyMediumEmphasized.copy(
                        fontStyle = if (cuteConversation.isSenderBlocked) FontStyle.Italic else FontStyle.Normal,
                        color = if (cuteConversation.read) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground,
                    )
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(end = 5.dp)
            ) {
                Text(
                    text = cuteConversation.date.toDate(),
                    style = MaterialTheme.typography.bodySmallEmphasized.copy(
                        color = if (cuteConversation.read) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onBackground
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    if (!cuteConversation.read) {
                        Badge(
                            containerColor = MaterialTheme.colorScheme.primary
                        ) {
                           // Text("99+")
                        }
                    }

                    if (cuteConversation.isSenderBlocked) {
                        IconButton(
                            onClick = { showUnblockDialog = true }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.block),
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }

    }
}





