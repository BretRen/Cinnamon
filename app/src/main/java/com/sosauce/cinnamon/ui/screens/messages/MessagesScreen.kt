@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.messages

import android.content.Intent
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.domain.model.CuteConversation
import com.sosauce.cinnamon.ui.navigation.Screen
import com.sosauce.cinnamon.ui.screens.messages.components.Conversation
import com.sosauce.cinnamon.ui.screens.messages.components.PinnedConversation
import com.sosauce.cinnamon.ui.screens.messages.components.dialogs.DeleteConversationsDialog
import com.sosauce.cinnamon.ui.shared_components.NoXFound
import com.sosauce.cinnamon.ui.shared_components.SelectedBar
import com.sosauce.cinnamon.ui.shared_components.searchbars.CuteSearchbar
import com.sosauce.sweetselect.rememberSweetSelectState

@Composable
fun MessagesScreen(
    state: MessagesState,
    onNavigate: (Screen) -> Unit,
    onHandleThreadsAction: (ThreadsAction) -> Unit
) {

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator()
        }
    } else {
        val context = LocalContext.current
        val listState = rememberLazyListState()
        var showDeleteConversationsDialog by remember { mutableStateOf(false) }
        val sweetSelectState = rememberSweetSelectState<CuteConversation>()
        

        Scaffold(
            bottomBar = {
                AnimatedContent(
                    targetState = sweetSelectState.isInSelectionMode,
                ) {
                    if (!it) {
                        CuteSearchbar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(),
                            sortingMenu = {},
                            fab = {
                                FloatingActionButton(
                                    onClick = { onNavigate(Screen.StartConversation) },
                                    shape = MaterialShapes.Cookie9Sided.toShape()
                                ) {
                                    Icon(
                                        painter = painterResource(R.drawable.add),
                                        contentDescription = null
                                    )
                                }
                            },
                            onNavigate = onNavigate
                        )
                    } else {
                        SelectedBar(
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(),
                            numberOfSelectedElements = sweetSelectState.selectedItems.size,
                            onClearSelected = sweetSelectState::clearSelected
                        ) {
                            IconButton(
                                onClick = {
                                    val threadIds = sweetSelectState.selectedItems.map { thread -> thread.threadId }

                                    onHandleThreadsAction(ThreadsAction.PinThreads(threadIds))
                                },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.pin_filled),
                                    contentDescription = "pin conversations"
                                )
                            }
                            IconButton(
                                onClick = {
                                    val threadIds = sweetSelectState.selectedItems.map { thread -> thread.threadId }

                                    onHandleThreadsAction(ThreadsAction.ArchiveThreads(threadIds))
                                    sweetSelectState.clearSelected()
                                },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.archive),
                                    contentDescription = "archive conversations"
                                )
                            }
                            IconButton(
                                onClick = { showDeleteConversationsDialog = true },
                                shapes = IconButtonDefaults.shapes()
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
        ) { paddingValues ->

            if (showDeleteConversationsDialog) {
                DeleteConversationsDialog(
                    onDismissRequest = { showDeleteConversationsDialog = false },
                    onDelete = {
                        val threadIds = sweetSelectState.selectedItems.map { it.threadId }
                        onHandleThreadsAction(ThreadsAction.DeleteThreads(threadIds))

                        showDeleteConversationsDialog = false
                    },
                    numberOfConversations = sweetSelectState.selectedItems.size
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = paddingValues,
                state = listState
            ) {
                if (state.hasArchivedThreads) {
                    item("archived") {
                        Card(
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(),
                            onClick = {
                                val intent = Intent("android.settings.MANAGE_APP_PROMOTED_NOTIFICATIONS")
                                context.startActivity(intent)
                                //onNavigate(Screen.ArchivedThreads)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.archived_outlined),
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(10.dp))
                                Text(stringResource(R.string.archived))
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                    }
                }
                item("pinned convos") {
                    LazyRow(modifier = Modifier.fillMaxWidth()) {
                        items(
                            items = state.pinnedConversations,
                            key = { it.threadId }
                        ) { cuteConversation ->

                            val isSelected by remember {
                                derivedStateOf { sweetSelectState.isSelected(cuteConversation) }
                            }

                            PinnedConversation(
                                cuteConversation = cuteConversation,
                                isSelected = isSelected,
                                onNavigate = { onNavigate(it) },
                                onLongClick = { sweetSelectState.toggle(cuteConversation) }
                            )
                        }
                    }
                }

                if (state.conversations.isNotEmpty()) {
                    items(
                        items = state.conversations,
                        key = { conversation -> conversation.threadId }
                    ) { conversation ->

                        val isSelected by remember {
                            derivedStateOf { sweetSelectState.isSelected(conversation) }
                        }

                        Conversation(
                            cuteConversation = conversation,
                            modifier = Modifier.animateItem(),
                            onClick = {
                                if (sweetSelectState.isInSelectionMode) {
                                    sweetSelectState.toggle(conversation)
                                } else {
                                    onNavigate(Screen.Conversation(conversation.threadId))
                                }
                            },
                            onLongClick = { sweetSelectState.toggle(conversation) },
                            backgroundColor = Color.Transparent,
                            isSelected = isSelected
                        )
                    }
                } else {
                    item {
                        NoXFound(
                            headlineText = R.string.no_convo_found,
                            bodyText = R.string.no_convo_found_desc,
                            icon = R.drawable.message_rounded
                        )
                    }
                }
            }
        }
    }
}
