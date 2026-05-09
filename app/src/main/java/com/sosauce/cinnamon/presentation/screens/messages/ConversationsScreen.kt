@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.presentation.screens.messages

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StampedPathEffectStyle.Companion.Morph
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.CornerRounding
import androidx.graphics.shapes.Morph
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.RoundedPolygon.Companion
import androidx.graphics.shapes.TransformResult
import androidx.graphics.shapes.circle
import androidx.graphics.shapes.star
import androidx.graphics.shapes.toPath
import androidx.graphics.shapes.transformed
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.data.datastore.rememberSortConversationsAscending
import com.sosauce.cinnamon.domain.model.CuteConversation
import com.sosauce.cinnamon.presentation.navigation.Screen
import com.sosauce.cinnamon.presentation.screens.messages.components.Conversation
import com.sosauce.cinnamon.presentation.screens.messages.components.PinnedConversation
import com.sosauce.cinnamon.presentation.shared_components.ConversationsSelectedBar
import com.sosauce.cinnamon.presentation.shared_components.NoXFound
import com.sosauce.cinnamon.presentation.shared_components.animations.AnimatedFab
import com.sosauce.cinnamon.presentation.shared_components.searchbars.CuteSearchbar
import com.sosauce.cinnamon.utils.LazyListKeys
import com.sosauce.cinnamon.utils.bouncySpec
import com.sosauce.cinnamon.utils.rememberInteractionSource
import com.sosauce.cinnamon.utils.selfAlignHorizontally
import com.sosauce.sweetselect.SweetSelectState
import com.sosauce.sweetselect.rememberSweetSelectState

@Composable
fun SharedTransitionScope.ConversationsScreen(
    state: ConversationsState,
    onNavigate: (Screen) -> Unit,
    onHandleConversationsAction: (ConversationsAction) -> Unit
) {
    val context = LocalContext.current
    val listState = rememberLazyListState()
    val sweetSelectState = rememberSweetSelectState<CuteConversation>()
    var sortConversationsAscending by rememberSortConversationsAscending()

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator()
        }
    } else {
        Scaffold(
            bottomBar = {
                AnimatedContent(
                    targetState = sweetSelectState.isInSelectionMode,
                ) {
                    if (!it) {
                        CuteSearchbar(
                            modifier = Modifier.selfAlignHorizontally(),
                            sortingMenu = {},
                            textFieldState = state.textFieldState,
                            fab = {
                                AnimatedFab(
                                    onClick = { onNavigate(Screen.StartConversation) }
                                )
                            },
                            onNavigate = onNavigate
                        )
                    } else {
                        ConversationsSelectedBar(
                            modifier = Modifier.selfAlignHorizontally(),
                            items = state.conversations,
                            multiSelectState = sweetSelectState,
                            onDeleteConversations = {
                                val threadIds = sweetSelectState.selectedItems.map { it.threadId }
                                onHandleConversationsAction(ConversationsAction.DeleteConversations(threadIds))
                                sweetSelectState.clearSelected()
                            },
                            onArchiveThreads = {
                                val threadIds = sweetSelectState.selectedItems.map { it.threadId }
                                onHandleConversationsAction(ConversationsAction.ArchiveConversations(threadIds))
                                sweetSelectState.clearSelected()
                            },
                            onPinThreads = {
                                val threadIds = sweetSelectState.selectedItems.map { it.threadId }
                                onHandleConversationsAction(ConversationsAction.PinConversations(threadIds))
                                sweetSelectState.clearSelected()
                            }
                        )
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = paddingValues,
                state = listState
            ) {
                if (state.hasArchivedThreads) {
                    item(LazyListKeys.ARCHIVED) {
                        Card(
                            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                            modifier = Modifier.selfAlignHorizontally(),
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

                threadsList(
                    pinnedThreads = state.pinnedConversations,
                    threads = state.conversations,
                    sweetSelectState = sweetSelectState,
                    onNavigate = onNavigate,
                    sharedTransitionScope = this@ConversationsScreen,
                    onHandleConversationsAction = onHandleConversationsAction,
                    emptyState = {
                        NoXFound(
                            headlineText = R.string.no_convo_found,
                            bodyText = R.string.no_convo_found_desc,
                            icon = R.drawable.message_rounded
                        )
                    }
                )
            }
        }
    }
}

fun LazyListScope.threadsList(
    pinnedThreads: List<CuteConversation>,
    threads: List<CuteConversation>,
    sweetSelectState: SweetSelectState<CuteConversation>,
    onNavigate: (Screen) -> Unit,
    sharedTransitionScope: SharedTransitionScope,
    emptyState: @Composable () -> Unit,
    onHandleConversationsAction: (ConversationsAction) -> Unit
) {
    item(LazyListKeys.PINNED_CONVERSATIONS) {
        LazyRow(modifier = Modifier.fillMaxWidth()) {
            items(
                items = pinnedThreads,
                key = { it.threadId }
            ) { conversation ->

                val isSelected by remember {
                    derivedStateOf { sweetSelectState.isSelected(conversation) }
                }

                PinnedConversation(
                    cuteConversation = conversation,
                    isSelected = isSelected,
                    onClick = {
                        if (sweetSelectState.isInSelectionMode) {
                            sweetSelectState.toggle(conversation)
                        } else {
                            onNavigate(Screen.Conversation(conversation.threadId))
                        }
                    },
                    onLongClick = { sweetSelectState.toggle(conversation) }
                )
            }
        }
    }

    with(sharedTransitionScope) {
        items(
            items = threads,
            key = { conversation -> conversation.threadId }
        ) { conversation ->


            val isSelected by remember {
                derivedStateOf { sweetSelectState.isSelected(conversation) }
            }

            Conversation(
                conversation = conversation,
                modifier = Modifier.animateItem(),
                onClick = {
                    if (sweetSelectState.isInSelectionMode) {
                        sweetSelectState.toggle(conversation)
                    } else {
                        onNavigate(Screen.Conversation(conversation.threadId))
                    }
                },
                onLongClick = { sweetSelectState.toggle(conversation) },
                isSelected = isSelected,
                onHandleConversationsAction = onHandleConversationsAction
            )
        }
    }
    if (threads.isEmpty() && pinnedThreads.isEmpty()) {
        item { emptyState() }
    }
}


