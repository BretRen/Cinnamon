@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class,
    ExperimentalHazeMaterialsApi::class
)

package com.sosauce.cuteconnect.ui.screens.messages.components

import android.net.Uri
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
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.ui.navigation.LocalHazeState
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.shared_components.CuteDropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.sosauce.cuteconnect.domain.states.ConversationState
import com.sosauce.cuteconnect.ui.screens.messages.ConversationDetailsState
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import com.sosauce.cuteconnect.ui.shared_components.DefaultContactIcon
import com.sosauce.cuteconnect.ui.shared_components.DefaultGroupChatIcon
import com.sosauce.cuteconnect.ui.shared_components.DropdownItemBlock
import com.sosauce.cuteconnect.ui.shared_components.DropdownItemDelete
import com.sosauce.cuteconnect.ui.shared_components.toolbars.ToolbarSkeleton
import com.sosauce.cuteconnect.utils.betterFormatNumber
import com.sosauce.cuteconnect.utils.getContactId
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.utils.rememberSearchbarMaxFloatValue
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi
import dev.chrisbanes.haze.materials.HazeMaterials
import kotlin.collections.firstOrNull
import kotlin.text.firstOrNull

@Composable
fun ConversationTopBar(
    modifier: Modifier = Modifier,
    state: ConversationDetailsState,
    onNavigateUp: () -> Unit,
    onHandleCallAction: (CallAction) -> Unit,
    onNavigate: (Screen) -> Unit,
    onDeleteConversation: () -> Unit
) {

    val context = LocalContext.current
    var showMoreMenu by remember { mutableStateOf(false) }
    val nameOrNumber = remember { state.recipients.first().betterFormatNumber() }
    val isGroupChat = state.recipients.size > 1



    ToolbarSkeleton(
        onClick = if (isGroupChat) { null } else {
            { onNavigate(Screen.ContactDetails(state.recipients.first().getContactId(context))) }
        }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onNavigateUp,
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = null
                )
            }
            if (isGroupChat) {
                DefaultGroupChatIcon(
                    modifier = Modifier.padding(end = 10.dp),
                )
            } else {
                DefaultContactIcon(
                    firstLetter = nameOrNumber.firstOrNull(),
                    modifier = Modifier.padding(end = 10.dp),
                    size = 38.dp,
                    contactPfp = Uri.EMPTY
                )
            }
            Text(
                text = buildString {
                    state.recipients.fastForEachIndexed { index, number ->
                        append(number.getContactNameOrNothing(context))
                        if (index != state.recipients.lastIndex) {
                            append(", ")
                        }
                    }
                },
                maxLines = 1,
                modifier = Modifier
                    .weight(1f),
                overflow = TextOverflow.Ellipsis
            )

            if (!isGroupChat) {
                IconButton(
                    onClick = {
                        onHandleCallAction(CallAction.LaunchCall(state.recipients.first()))
                    },
                    shapes = IconButtonDefaults.shapes()
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = null
                    )
                }
            }
            IconButton(
                onClick = { showMoreMenu = true },
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    imageVector = Icons.Rounded.MoreVert,
                    contentDescription = null
                )
                DropdownMenu(
                    expanded = showMoreMenu,
                    onDismissRequest = { showMoreMenu = false },
                    shape = RoundedCornerShape(24.dp),
//                    modifier = Modifier
//                        .hazeEffect(
//                            state = LocalHazeState.current,
//                            style = HazeMaterials.regular(
//                                containerColor = MaterialTheme.colorScheme.surfaceContainer
//                            )
//                        )
                ) {
                    CuteDropdownMenuItem(
                        onClick = { onNavigate(Screen.ConversationTheming(state.threadId)) },
                        text = { Text("Customize chat") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Outlined.Palette,
                                contentDescription = null
                            )
                        }
                    )

                    DropdownItemBlock(
                        onBlock = {},
                        dialogText = { Text(stringResource(R.string.block)) },
                    )
                    DropdownItemDelete(
                        onDelete = onDeleteConversation,
                        dialogTitle = { Text(stringResource(R.string.delete_convo)) },
                        dialogText = { Text(stringResource(R.string.delete_convo_u_sure)) }
                    )
                }
            }

        }
    }
}