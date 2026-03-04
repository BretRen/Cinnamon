@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.datastore.rememberArchivedConversations
import com.sosauce.cuteconnect.data.datastore.rememberPinnedConversations
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.messages.components.Conversation
import com.sosauce.cuteconnect.ui.screens.messages.components.PinnedConversation
import com.sosauce.cuteconnect.ui.shared_components.NoXFound
import com.sosauce.cuteconnect.ui.shared_components.SelectedBar
import com.sosauce.cuteconnect.ui.shared_components.searchbars.CuteSearchbar
import com.sosauce.cuteconnect.utils.CuteRoundedCornerShape
import com.sosauce.cuteconnect.utils.addOrRemove
import com.sosauce.cuteconnect.utils.copyMutate
import com.sosauce.sweetselect.rememberSweetSelectState

@Composable
fun MessagesScreen(
    state: MessagesState,
    onNavigate: (Screen) -> Unit,
    onDeleteConversation: (Long) -> Unit
) {

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator()
        }
    } else {
        val listState = rememberLazyListState()
        var pinnedConversations by rememberPinnedConversations()
        var archivedConversations by rememberArchivedConversations()
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
                                    sweetSelectState.selectedItems.forEach {
                                        pinnedConversations = pinnedConversations.copyMutate { addOrRemove(it.threadId.toString()) }
                                    }
                                    sweetSelectState.clearSelected()
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
                                    sweetSelectState.selectedItems.forEach {
                                        archivedConversations = archivedConversations.copyMutate { addOrRemove(it.threadId.toString()) }
                                    }
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
                AlertDialog(
                    onDismissRequest = { showDeleteConversationsDialog = false },
                    icon = {
                        Image(
                            painter = painterResource(R.drawable.delete_filled),
                            contentDescription = null
                        )
                    },
                    title = { Text("Delete conversations") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                sweetSelectState.selectedItems.forEach { onDeleteConversation(it.threadId) }
                                showDeleteConversationsDialog = false
                            },
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Text(stringResource(R.string.delete))
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDeleteConversationsDialog = false },
                            shapes = ButtonDefaults.shapes()
                        ) {
                            Text(stringResource(R.string.cancel))
                        }
                    },
                    text = {
                        Text("Are you sure you want to delete ${sweetSelectState.selectedItems.size} conversations ? This cannot be undone!")
                    }
                )
            }


            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = paddingValues,
                state = listState
            ) {
                if (archivedConversations.isNotEmpty()) {
                    item("archived") {
                        Card(
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(),
                            onClick = { onNavigate(Screen.ArchivedThreads) }
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
