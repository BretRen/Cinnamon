@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.messages

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.datastore.rememberArchivedConversations
import com.sosauce.cuteconnect.data.datastore.rememberPinnedConversations
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.messages.components.Conversation
import com.sosauce.cuteconnect.ui.screens.messages.components.PinnedConversation
import com.sosauce.cuteconnect.ui.shared_components.SelectedBar
import com.sosauce.cuteconnect.ui.shared_components.searchbars.CuteSearchbar
import com.sosauce.cuteconnect.utils.CuteRoundedCornerShape
import com.sosauce.cuteconnect.utils.addOrRemove
import com.sosauce.cuteconnect.utils.copyMutate

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
        val selectedConversations = remember { mutableStateListOf<Long>() }
        var showDeleteConversationsDialog by remember { mutableStateOf(false) }
        

        Scaffold(
            bottomBar = {
//            AnimatedContent(
//                targetState = selectedConversations.isEmpty(),
//                transitionSpec = { scaleIn() togetherWith scaleOut() },
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .wrapContentWidth()
//            ) {
//            }
                if (selectedConversations.isEmpty()) {
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
                        numberOfSelectedElements = selectedConversations.size,
                        onClearSelected = selectedConversations::clear
                    ) {
                        IconButton(
                            onClick = {
                                selectedConversations.fastForEach {
                                    pinnedConversations = pinnedConversations.copyMutate { addOrRemove(it.toString()) }
                                }
                                selectedConversations.clear()
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
                                selectedConversations.fastForEach {
                                    archivedConversations = archivedConversations.copyMutate { addOrRemove(it.toString()) }
                                }
                                selectedConversations.clear()
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
                                selectedConversations.fastForEach(onDeleteConversation)
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
                        Text("Are you sure you want to delete ${selectedConversations.size} conversations ? This cannot be undone!")
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
                    LazyRow {
                        items(
                            items = state.pinnedConversations,
                            key = { it.threadId }
                        ) { cuteConversation ->
                            PinnedConversation(
                                cuteConversation = cuteConversation,
                                onNavigate = { onNavigate(it) },
                                onLongClick = { selectedConversations.addOrRemove(cuteConversation.threadId) }
                            )
                        }
                    }
                }
                itemsIndexed(
                    items = state.conversations,
                    key = { _, conversation -> conversation.threadId }
                ) { index, conversation ->
                    Conversation(
                        cuteConversation = conversation,
                        modifier = Modifier.animateItem(),
                        onClick = { onNavigate(Screen.Conversation(conversation.threadId)) },
                        shape = CuteRoundedCornerShape(
                            top = if (index == 0) 24.dp else 4.dp,
                            bottom = if (index == state.conversations.lastIndex) 24.dp else 0.dp
                        )
                        //onLongClick = { selectedConversations.addOrRemove(cuteConversation.threadId) },
                    )
                }
            }
        }
    }
}
