@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class,
    ExperimentalHazeMaterialsApi::class
)

package com.sosauce.cuteconnect.ui.screens.messages

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.materialkolor.DynamicMaterialExpressiveTheme
import com.materialkolor.PaletteStyle
import com.materialkolor.dynamiccolor.ColorSpec
import com.materialkolor.rememberDynamicMaterialThemeState
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingActions
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.messages.components.ConversationBottomBar
import com.sosauce.cuteconnect.ui.screens.messages.components.ConversationTopBar
import com.sosauce.cuteconnect.ui.screens.messages.components.MessageBubble
import com.sosauce.cuteconnect.ui.screens.messages.components.SandwichPosition
import com.sosauce.cuteconnect.ui.screens.messages.components.SelectedTopBar
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import com.sosauce.cuteconnect.utils.ImageUtils
import com.sosauce.cuteconnect.utils.addOrRemove
import com.sosauce.cuteconnect.utils.rememberHazeState
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
        val selectedMessages = remember { mutableStateListOf<CuteMessage>() }
        val listState = rememberLazyListState()
        val chatWallpaperState = rememberHazeState()

        LaunchedEffect(state.messages) { listState.scrollToItem(listState.layoutInfo.totalItemsCount) }



        DynamicMaterialExpressiveTheme(
            motionScheme = MotionScheme.expressive(),
            state = rememberDynamicMaterialThemeState(
                seedColor = if (state.settings.color == -1) MaterialTheme.colorScheme.primary else Color(state.settings.color),
                isDark = isSystemInDarkTheme(), // TODO NEED TO CHANGE THIS WHEN I HAVE APP THEME
                specVersion = ColorSpec.SpecVersion.SPEC_2025,
                style = PaletteStyle.Vibrant
            ),
        ) {

            Box {
                // Wallpaper
                AsyncImage(
                    model = ImageUtils.imageRequester(state.settings.wallpaper.toUri(), context),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .hazeSource(chatWallpaperState)
                )

                Scaffold(
                    topBar = {
                        AnimatedContent(
                            targetState = selectedMessages.isEmpty()
                        ) {
                            if (it) {
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
                            } else {
                                SelectedTopBar(
                                    selectedCuteMessages = selectedMessages,
                                    onUnselectAll = selectedMessages::clear
                                )
                            }
                        }
                    },
                    bottomBar = {
                        ConversationBottomBar(
                            onSendMessage = { message ->
                                onHandleConversationActions(
                                    ConversationActions.SendSms(
                                        address = state.recipients.first(),
                                        message = message
                                    )
                                )
                            },
                            onSaveDraft = { draft ->
                                onHandleConversationSettingsActions(
                                    ConversationSettingActions.UpsertConversationSettings(
                                        state.settings.copy(
                                            draft = draft
                                        )
                                    )
                                )
                            },
                            cuteSimCards = state.simCards
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
                        contentPadding = paddingValues
                    ) {


                        state.messages.forEach { (date, cuteMessages) ->
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

                            itemsIndexed(
                                items = cuteMessages,
                                key = { _, cuteMessage -> cuteMessage.id }
                            ) { index, cuteMessage ->


                                val prev = cuteMessages.getOrNull(index - 1)
                                val next = cuteMessages.getOrNull(index + 1)
                                val sameAsPrev = prev?.type == cuteMessage.type
                                val sameAsNext = next?.type == cuteMessage.type

                                val sandwichPosition = when {
                                    !sameAsPrev && !sameAsNext -> SandwichPosition.SOLO
                                    !sameAsPrev && sameAsNext -> SandwichPosition.TOP
                                    sameAsPrev && sameAsNext -> SandwichPosition.MIDDLE
                                    sameAsPrev && !sameAsNext -> SandwichPosition.BOTTOM
                                    else -> SandwichPosition.SOLO
                                }
                                MessageBubble(
                                    modifier = Modifier.animateItem(),
                                    cuteMessage = cuteMessage,
                                    onAddMessageToSelected = { selectedMessages.addOrRemove(cuteMessage) },
                                    isSelected = selectedMessages.contains(cuteMessage),
                                    isInSelectMode = selectedMessages.isNotEmpty(),
                                    sandwichPosition = sandwichPosition,
                                    allMedias = emptyList(),
                                    onHandleConversationActions = onHandleConversationActions
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}