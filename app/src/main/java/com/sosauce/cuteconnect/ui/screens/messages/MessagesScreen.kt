@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirstOrNull
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.messages.components.Conversation
import com.sosauce.cuteconnect.ui.shared_components.CuteSearchbar
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.ConversationSettings
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.shared_components.SelectedBar
import com.sosauce.cuteconnect.utils.addOrRemove
import com.sosauce.cuteconnect.utils.rememberSearchbarAlignment
import com.sosauce.cuteconnect.utils.showCuteSearchbar
import com.sosauce.cuteconnect.viewModels.ConversationViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun MessagesScreen(
    conversations: List<CuteConversation>,
    cuteContacts: List<CuteContact>,
    onNavigate: (Screen) -> Unit,
    onEditPinnedConvos: (String) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val conversationViewModel = koinViewModel<ConversationViewModel>()
    val pinnedConvos by conversationViewModel.getPinnedConversations().collectAsStateWithLifecycle()

    val selectedConversations = remember { mutableStateListOf<CuteConversation>() }
    Scaffold { paddingValues ->
        Box(Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = paddingValues,
                state = listState
            ) {
//            item {
//                Row(Modifier.fillMaxWidth().clickable { onNavigate(Screen.DebugMms) }.statusBarsPadding()) {
//                    CuteText("All Mms Debug")
//                }
//            }
//                item("pinned convos") {
//                    LazyRow {
//                        items(
//                            items = allConversations.fastFilter { it.threadId in pinnedConvos },
//                            key = { it.threadId }
//                        ) { cuteConversation ->
//                            PinnedConversation(
//                                cuteConversation = cuteConversation,
//                                onNavigate = { screen ->
//                                    if (selectedConversations.isEmpty()) {
//                                        onNavigate(screen)
//                                    } else {
//                                        selectedConversations.addOrRemove(cuteConversation)
//                                    }
//                                },
//                                onLongClick = { selectedConversations.addOrRemove(cuteConversation) },
//                                isSelected = selectedConversations.contains(cuteConversation)
//                            )
//                        }
//                    }
//                }
                items(
                    items = conversations,
                    key = { it.threadId }
                ) { cuteConversation ->
                    //val conversationSettings by conversationViewModel.getConversationSettings(cuteConversation.threadId).collectAsStateWithLifecycle(ConversationSettings())

                    Conversation(
                        cuteConversation = cuteConversation,
                        conversationSettings = ConversationSettings(),
                        cuteContact = cuteContacts.fastFirstOrNull { it.id in cuteConversation.contactsId }, // If user has multiple contacts with same number, then not our problem duh
                        modifier = Modifier
                            .animateItem()
                            .padding(
                                vertical = 4.dp,
                                horizontal = 2.dp
                            )
                            .background(
                                color = if (selectedConversations.contains(cuteConversation)) MaterialTheme.colorScheme.surfaceContainerHighest else Color.Transparent,
                                shape = RoundedCornerShape(24.dp)
                            ),
                        onClick = { screen ->
                            if (selectedConversations.isEmpty()) {
                                onNavigate(screen)
                            } else {
                                selectedConversations.addOrRemove(cuteConversation)
                            }
                        },
                        onLongClick = { selectedConversations.addOrRemove(cuteConversation) }
                    )
                }
            }

            AnimatedVisibility(
                visible = listState.showCuteSearchbar,
                modifier = Modifier.align(rememberSearchbarAlignment()),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {

                AnimatedContent(
                    targetState = selectedConversations.isEmpty(),
                    transitionSpec = { scaleIn() togetherWith scaleOut() }
                ) {
                    if (it) {
                        CuteSearchbar(
                            trailingIcon = {
                                Row {
                                    IconButton(
                                        onClick = {}
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.ArrowOutward,
                                            contentDescription = null
                                        )
                                    }
                                    IconButton(
                                        onClick = {}
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Settings,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },
                            fab = {
                                SmallFloatingActionButton(
                                    onClick = { onNavigate(Screen.StartConversation) }
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Add,
                                        contentDescription = null
                                    )
                                }
                            },
                            onNavigate = onNavigate
                        )
                    } else {
                        SelectedBar(
                            numberOfSelectedElements = selectedConversations.size,
                            onClearSelected = selectedConversations::clear
                        ) {
                            IconButton(
                                onClick = {
                                    selectedConversations.fastForEach {
//                                        scope.launch {
//                                            val convoSettings = conversationViewModel.getConversationSettings(it.threadId).first() ?: ConversationSettings(it.threadId) // Avoid having to always copy the Id
//
//                                            conversationViewModel.handleConversationSettingsActions(
//                                                ConversationSettingActions.UpsertConversationSettings(
//                                                    convoSettings.copy(
//                                                        convoId = it.threadId,
//                                                        isPinned = true
//                                                    )
//                                                )
//                                            )
//                                        }
                                    }
                                    selectedConversations.clear()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pin_filled),
                                    contentDescription = "pin conversations"
                                )
                            }
                            IconButton(
                                onClick = { /* archive selected convos */ }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.archive),
                                    contentDescription = "archive conversations"
                                )
                            }
                            IconButton(
                                onClick = { /* have a confirmation dialog */ }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_filled),
                                    contentDescription = "delete conversations",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}