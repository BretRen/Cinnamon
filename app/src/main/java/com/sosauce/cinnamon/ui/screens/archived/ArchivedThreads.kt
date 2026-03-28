@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.ui.screens.archived

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.domain.model.CuteConversation
import com.sosauce.cinnamon.ui.navigation.Screen
import com.sosauce.cinnamon.ui.screens.messages.ThreadsAction
import com.sosauce.cinnamon.ui.screens.messages.components.Conversation
import com.sosauce.cinnamon.ui.screens.messages.components.dialogs.DeleteConversationsDialog
import com.sosauce.cinnamon.ui.shared_components.SelectedBar
import com.sosauce.cinnamon.ui.shared_components.searchbars.CuteSearchbar
import com.sosauce.sweetselect.rememberSweetSelectState

@Composable
fun ArchivedThreads(
    state: ArchivedState,
    onNavigate: (Screen) -> Unit,
    onNavigateUp: () -> Unit,
    onHandleThreadsAction: (ThreadsAction) -> Unit
) {


    val sweetSelectState = rememberSweetSelectState<CuteConversation>()
    var showDeleteConversationsDialog by remember { mutableStateOf(false) }

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
        LazyColumn(
            contentPadding = paddingValues
        ) {
            items(
                items = state.threads,
                key = { it.threadId }
            ) { thread ->
                Conversation(
                    cuteConversation = thread,
                    onClick = { onNavigate(Screen.Conversation(thread.threadId)) },
                    backgroundColor = Color.Transparent
                    //onLongClick = {}
                )
            }
        }

    }
}