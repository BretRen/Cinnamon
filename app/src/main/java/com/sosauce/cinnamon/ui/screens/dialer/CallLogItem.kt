@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.dialer

import android.content.ClipData
import android.provider.CallLog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuGroup
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.DropdownMenuPopup
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEachIndexed
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.domain.model.CuteCallLog
import com.sosauce.cinnamon.ui.navigation.Screen
import com.sosauce.cinnamon.ui.screens.phone.CallAction
import com.sosauce.cinnamon.ui.shared_components.DefaultContactIcon
import com.sosauce.cinnamon.utils.getContactId
import com.sosauce.cinnamon.utils.getContactPfpUriFromId
import com.sosauce.cinnamon.utils.getItemShape
import com.sosauce.cinnamon.utils.getThreadIdOrCreate
import com.sosauce.cinnamon.utils.secondsToDuration
import com.sosauce.cinnamon.utils.toTime
import com.sosauce.sweetselect.SweetSelectState
import com.sosauce.sweetselect.sweetClickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun CallLogItem(
    modifier: Modifier = Modifier,
    callLog: CuteCallLog,
    numberOfAppearance: Int,
    onCallAction: (CallAction) -> Unit,
    onNavigate: (Screen) -> Unit,
    shape: Shape = RoundedCornerShape(24.dp),
    onDeleteCallLog: () -> Unit,
    sweetSelectState: SweetSelectState<CuteCallLog>
) {

    val context = LocalContext.current
    val clipboard = LocalClipboard.current
    val scope = rememberCoroutineScope()
    var showMoreOptions by remember { mutableStateOf(false) }

    val iconColor = when(callLog.callType) {
        CallLog.Calls.INCOMING_TYPE, CallLog.Calls.MISSED_TYPE -> Color.Red.copy(0.85f)
        CallLog.Calls.OUTGOING_TYPE -> Color.Green.copy(0.85f)
        CallLog.Calls.REJECTED_TYPE -> Color.Cyan.copy(0.85f)
        else -> LocalContentColor.current
    }

    val icon = when(callLog.callType) {
        CallLog.Calls.INCOMING_TYPE, CallLog.Calls.MISSED_TYPE -> R.drawable.call_missed
        CallLog.Calls.OUTGOING_TYPE -> R.drawable.call_outgoing
        CallLog.Calls.REJECTED_TYPE -> R.drawable.call_rejected
        else -> R.drawable.call_missed
    }

    val actions = listOf(
        CallLogAction(
            onClick = {
                onCallAction(CallAction.LaunchCall(callLog.rawNumber))
                showMoreOptions = false
            },
            icon = R.drawable.phone,
            text = R.string.call
        ),
        CallLogAction(
            onClick = {
                scope.launch(Dispatchers.IO) {
                    val threadId = callLog.rawNumber.getThreadIdOrCreate(context)
                    onNavigate(Screen.Conversation(threadId))
                    showMoreOptions = false
                }
            },
            icon = R.drawable.message_rounded,
            text = R.string.send_msg
        ),
        CallLogAction(
            onClick = {
                scope.launch {
                    clipboard.setClipEntry(
                        ClipEntry(
                            ClipData.newPlainText(callLog.rawNumber, callLog.rawNumber)
                        )
                    )
                    Toast.makeText(context, "Copied!", Toast.LENGTH_SHORT).show()
                    showMoreOptions = false
                }
            },
            icon = R.drawable.copy,
            text = R.string.copy_number
        ),
        CallLogAction(
            onClick = onDeleteCallLog,
            text = R.string.delete,
            icon = R.drawable.delete,
            tint = MaterialTheme.colorScheme.error
        )
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(
                vertical = 1.dp,
                horizontal = 5.dp
            )
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .sweetClickable(
                item = callLog,
                state = sweetSelectState,
                onClick = { onCallAction(CallAction.LaunchCall(callLog.rawNumber)) }
            )
    ) {
        Row(
            modifier = Modifier.padding(vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DefaultContactIcon(
                firstLetter = callLog.beautifiedNumberOrName.firstOrNull(),
                modifier = Modifier.padding(start = 10.dp),
                contactPfp = callLog.rawNumber.getContactId(context).getContactPfpUriFromId()
            )
            Column(
                modifier = Modifier
                    .padding(start = 15.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = if (numberOfAppearance <= 1) callLog.beautifiedNumberOrName else "${callLog.beautifiedNumberOrName} ($numberOfAppearance)",
                    maxLines = 1,
                    modifier = Modifier.basicMarquee()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = painterResource(icon),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = buildString {
                            append(callLog.date.toTime())
                            if (callLog.duration > 0 && (callLog.callType == CallLog.Calls.INCOMING_TYPE || callLog.callType == CallLog.Calls.OUTGOING_TYPE)) {
                                append(" · ")
                                append(callLog.duration.secondsToDuration())
                            }
                        },
                        modifier = Modifier.basicMarquee(),
                        style = MaterialTheme.typography.bodyMediumEmphasized.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    callLog.country?.let { country ->
                        Spacer(Modifier.weight(1f))
                        Text(
                            text = country,
                            style = MaterialTheme.typography.bodySmallEmphasized.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            Row {
                IconButton(
                    onClick = { showMoreOptions = true }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = null
                    )
                }

                DropdownMenuPopup(
                    expanded = showMoreOptions,
                    onDismissRequest = { showMoreOptions = false }
                ) {
                    DropdownMenuGroup(
                        shapes = MenuDefaults.groupShapes()
                    ) {
                        actions.fastForEachIndexed { index, action ->
                            DropdownMenuItem(
                                onClick = action.onClick,
                                shape = MenuDefaults.getItemShape(index, actions.lastIndex),
                                text = {
                                    Text(
                                        text = stringResource(action.text),
                                        color = action.tint ?: LocalContentColor.current
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        painter = painterResource(action.icon),
                                        contentDescription = null,
                                        tint = action.tint ?: LocalContentColor.current
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

    }

}

private data class CallLogAction(
    val onClick: () -> Unit,
    val icon: Int,
    val text: Int,
    val tint: Color? = null
)