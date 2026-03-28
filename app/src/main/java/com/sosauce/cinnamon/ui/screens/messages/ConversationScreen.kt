@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class,
    ExperimentalHazeMaterialsApi::class
)

package com.sosauce.cinnamon.ui.screens.messages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.retain.RetainedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil3.compose.AsyncImage
import com.sosauce.cinnamon.data.conversation_settings.ConversationSettingActions
import com.sosauce.cinnamon.data.managers.ActiveThreadId
import com.sosauce.cinnamon.domain.model.CuteMessage
import com.sosauce.cinnamon.ui.navigation.Screen
import com.sosauce.cinnamon.ui.screens.messages.components.bottombar.ConversationBottomBar
import com.sosauce.cinnamon.ui.screens.messages.components.topbars.ConversationTopBar
import com.sosauce.cinnamon.ui.screens.messages.components.bubble.MessageBubble
import com.sosauce.cinnamon.ui.screens.messages.components.bubble.SandwichPosition
import com.sosauce.cinnamon.ui.screens.messages.components.topbars.SelectedTopBar
import com.sosauce.cinnamon.ui.screens.phone.CallAction
import com.sosauce.cinnamon.utils.getAdaptivePrimaryColor
import com.sosauce.cinnamon.utils.rememberHazeState
import com.sosauce.sweetselect.rememberSweetSelectState
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    state: ConversationDetailsState,
    onHandleCallAction: (CallAction) -> Unit,
    onDeleteConversation: () -> Unit,
    onHandleConversationSettingsActions: (ConversationSettingActions) -> Unit,
    onHandleConversationActions: (ConversationActions) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigate: (Screen) -> Unit,
    onUpdateSeedColor: (Color) -> Unit
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
        val chatWallpaperState = rememberHazeState()
        val sweetSelectState = rememberSweetSelectState<CuteMessage>()
        val primary = MaterialTheme.colorScheme.primary
        val lifecycleOwner = LocalLifecycleOwner.current


        RetainedEffect(state.settings.color) {
            if (state.settings.color != -1) {
                onUpdateSeedColor(Color(state.settings.color))
            }
            onRetire {
                onUpdateSeedColor(context.getAdaptivePrimaryColor(primary))
            }
        }

        RetainedEffect(lifecycleOwner) {
            val observer = LifecycleEventObserver { _, event ->
                when(event) {
                    Lifecycle.Event.ON_CREATE, Lifecycle.Event.ON_RESUME, Lifecycle.Event.ON_START -> ActiveThreadId.threadId = state.threadId
                    else -> ActiveThreadId.threadId = null
                }
            }
            lifecycleOwner.lifecycle.addObserver(observer)
            onRetire { lifecycleOwner.lifecycle.removeObserver(observer) }
        }

        LaunchedEffect(state.messages) {
            listState.animateScrollToItem(0)
        }

        Box {
            // Wallpaper
            AsyncImage(
                model = state.settings.wallpaper.toUri(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(chatWallpaperState)
            )

            Scaffold(
                topBar = {
                    AnimatedContent(
                        targetState = sweetSelectState.isInSelectionMode
                    ) {
                        if (it) {
                            SelectedTopBar(
                                selectedCuteMessages = sweetSelectState.selectedItems.toList(),
                                onSelectAll = { sweetSelectState.toggleAll(state.messages.values.flatten()) },
                                onUnselectAll = sweetSelectState::clearSelected
                            )
                        } else {
                            ConversationTopBar(
                                state = state,
                                onNavigateUp = onNavigateUp,
                                onHandleCallAction = onHandleCallAction,
                                onNavigate = onNavigate,
                                onDeleteConversation = onDeleteConversation,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth()
                            )
                        }
                    }
                },
                bottomBar = {
                    ConversationBottomBar(
                        conversationState = state,
                        onSaveDraft = { draft ->
                            onHandleConversationSettingsActions(
                                ConversationSettingActions.UpsertConversationSettings(
                                    state.settings.copy(
                                        draft = draft
                                    )
                                )
                            )
                        },
                        onHandleConversationActions = onHandleConversationActions
                    )
                }
            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        //.hazeSource(LocalHazeState.current)
                        .hazeEffect(
                            state = chatWallpaperState
                        ) { blurRadius = state.settings.wallpaperBlurIntensity.dp },
                    state = listState,
                    contentPadding = paddingValues,
                    reverseLayout = true
                ) {


                    state.messages.forEach { (date, cuteMessages) ->
                        itemsIndexed(
                            items = cuteMessages,
                            key = { _, cuteMessage -> "${cuteMessage.date}_${cuteMessage.id}" }
                        ) { index, cuteMessage ->


                            val prev = cuteMessages.getOrNull(index + 1)
                            val next = cuteMessages.getOrNull(index - 1)
                            val sameAsPrev = prev?.type == cuteMessage.type
                            val sameAsNext = next?.type == cuteMessage.type

                            val sandwichPosition = when {
                                !sameAsPrev && !sameAsNext -> SandwichPosition.SOLO
                                !sameAsPrev && sameAsNext -> SandwichPosition.TOP
                                sameAsPrev && sameAsNext -> SandwichPosition.MIDDLE
                                sameAsPrev && !sameAsNext -> SandwichPosition.BOTTOM
                                else -> SandwichPosition.SOLO
                            }

                            val isSelected by sweetSelectState.isSelectedAsState(cuteMessage)

                            MessageBubble(
                                modifier = Modifier.animateItem(),
                                cuteMessage = cuteMessage,
                                isSelected = isSelected,
                                sandwichPosition = sandwichPosition,
                                onHandleConversationActions = onHandleConversationActions,
                                sweetSelectState = sweetSelectState
                            )
                        }
                        item(
                            key = date
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = date,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.background,
                                            shape = RoundedCornerShape(50)
                                        )
                                        .padding(5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}