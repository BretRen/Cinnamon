@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class,
    ExperimentalMaterial3ExpressiveApi::class, ExperimentalLayoutApi::class
)

package com.sosauce.cuteconnect.ui.screens.messages

import android.net.Uri
import android.provider.Telephony.Sms
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.content.consume
import androidx.compose.foundation.content.contentReceiver
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.SimCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEach
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.sosauce.cuteconnect.data.actions.CommonAction
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.messages.components.ConversationTopBar
import com.sosauce.cuteconnect.ui.screens.messages.components.MessageBubble
import com.sosauce.cuteconnect.ui.screens.messages.components.SelectedTopBar
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.utils.ImageUtils
import com.sosauce.cuteconnect.utils.cuteHazeEffect
import com.sosauce.cuteconnect.viewModels.ConversationViewModel
import dev.chrisbanes.haze.hazeSource
import org.koin.androidx.compose.koinViewModel
import androidx.core.net.toUri
import com.materialkolor.DynamicMaterialTheme
import com.materialkolor.rememberDynamicMaterialThemeState
import com.sosauce.cuteconnect.data.actions.CallAction
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingActions
import com.sosauce.cuteconnect.data.datastore.rememberDefaultSimCard
import com.sosauce.cuteconnect.domain.model.ConversationSettings
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.domain.model.CuteSimCard
import com.sosauce.cuteconnect.ui.screens.messages.components.ActionPicker
import com.sosauce.cuteconnect.ui.screens.messages.components.GenericThumbnail
import com.sosauce.cuteconnect.ui.screens.messages.components.MediaThumbnail
import com.sosauce.cuteconnect.ui.screens.messages.components.SandwichPosition
import com.sosauce.cuteconnect.ui.shared_components.SimSelector
import com.sosauce.cuteconnect.utils.addOrNot
import com.sosauce.cuteconnect.utils.addOrRemove
import com.sosauce.cuteconnect.utils.rememberHazeState
import com.sosauce.cuteconnect.utils.toReadableDate

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ConversationScreen(
    cuteMessages: List<CuteMessage>,
    cuteConversation: CuteConversation,
    cuteSimCards: List<CuteSimCard>,
    threadId: Long,
    onHandleCommonAction: (CommonAction) -> Unit,
    onHandleCallAction: (CallAction) -> Unit,
    onNavigateUp: () -> Unit,
    onNavigate: (Screen) -> Unit,
) {
    val context = LocalContext.current
    val convoViewModel = koinViewModel<ConversationViewModel>()
    val convoSettings by convoViewModel.getConversationSettings(threadId).collectAsStateWithLifecycle(ConversationSettings(threadId))
    val allMessages = remember(cuteMessages) { cuteMessages.sortedBy { it.date }.groupBy { it.date.toReadableDate() }}
    var selectedMessages = remember { mutableStateListOf<CuteMessage>() }

    val listState = rememberLazyListState()
    val hazeState = rememberHazeState()

    LaunchedEffect(cuteMessages) {
        cuteMessages.fastForEach {
            if (!it.read) {
                onHandleCommonAction(CommonAction.MarkMessageAsRead(it.id))
            }
        }
    }


    LaunchedEffect(cuteMessages) {
        // Don't use animateScrollToItem because when initially loading in the screen, the user will actually see the scrolling, we don't want that
        listState.scrollToItem(listState.layoutInfo.totalItemsCount)
    }

    DynamicMaterialTheme(
        state = rememberDynamicMaterialThemeState(
            seedColor = if (convoSettings.color == 0) MaterialTheme.colorScheme.primary else Color(convoSettings.color),
            isDark = true // NEED TO CHANGE THIS WHEN I HAVE APP THEME
        ),
    ) {

        Box {
            // wallpaper
            AsyncImage(
                model = ImageUtils.imageRequester(convoSettings.wallpaper.toUri(), context),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .hazeSource(hazeState)
            )

            Scaffold(
                topBar = {
                    AnimatedContent(
                        targetState = selectedMessages.isEmpty(),
                        transitionSpec = { scaleIn() togetherWith scaleOut() }
                    ) {
                        if (it) {
                            ConversationTopBar(
                                cuteConversation = cuteConversation,
                                threadId = threadId,
                                onNavigateUp = onNavigateUp,
                                onHandleCallAction = onHandleCallAction,
                                onNavigate = onNavigate,
                                onDeleteConversation = {
                                    cuteMessages.fastForEach { message ->
                                        onHandleCommonAction(
                                            CommonAction.DeleteFromContentUri(
                                                Sms.CONTENT_URI,
                                                message.id
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentWidth()
                            )
                        } else {
                            SelectedTopBar(
                                selectedCuteMessages = selectedMessages,
                                onUnselectAll = selectedMessages::clear,
                                onHandleCommonAction = onHandleCommonAction
                            )
                        }
                    }
                },
                bottomBar = {
//                    if (isSenderShortCode) {
//                        ShortCodeBottomBar()
//                    } else {
//                    }
//                    TextingUnavailableBar(TextingUnavailableReason.AIRPLANE_MODE_ON)
                    MessageTextActions(
                        onSendMessage = { message ->
                            val cuteMessage = CuteMessage(
                                body = message,
                                type = Sms.MESSAGE_TYPE_SENT,
                                address = cuteConversation.recipients.first(),
                                threadId = threadId,
                            )
                            onHandleCommonAction(CommonAction.SendMessage(cuteMessage))
                        },
                        conversationSettings = convoSettings,
                        onEditConversationSettings = { convoViewModel.handleConversationSettingsActions(it) },
                        cuteSimCards = cuteSimCards
                    )
                },
                containerColor = Color.Transparent

            ) { paddingValues ->
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .cuteHazeEffect(
                            state = hazeState,
                            intensity = convoSettings.wallpaperBlurIntensity
                        ),
                    state = listState,
                    contentPadding = paddingValues
                ) {
                    allMessages.forEach { (date, cuteMessages) ->
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp),
                                horizontalArrangement = Arrangement.Center
                            ) {
                                CuteText(
                                    text = date,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier
                                        .background(
                                            color = MaterialTheme.colorScheme.background,
                                            shape = RoundedCornerShape(10.dp)
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
                                allMedias = emptyList()
                            )

                        }
                    }
                }
            }
        }
    }




}

@Composable
private fun MessageTextActions(
    conversationSettings: ConversationSettings,
    onEditConversationSettings: (ConversationSettingActions) -> Unit,
    onSendMessage: (message: String) -> Unit,
    cuteSimCards: List<CuteSimCard>,
) {

    val context = LocalContext.current
    var value by remember { mutableStateOf("") }
    val textFieldState = rememberTextFieldState()
    val mediasToSend = rememberSaveable { mutableStateListOf<Uri>() }
    var isActionPickerExpanded by remember { mutableStateOf(false) }
    val defaultSimCard by rememberDefaultSimCard()
    var simSelectorVisible by remember { mutableStateOf(false) }

    SimSelector(
        visible = simSelectorVisible,
        onDismissRequest = { simSelectorVisible = false },
        cuteSimCards = cuteSimCards
    )

    DisposableEffect(Unit) {
        onDispose {
            if (value.isNotEmpty()) {
                onEditConversationSettings(
                    ConversationSettingActions.UpsertConversationSettings(
                        conversationSettings.copy(
                            draft = value
                        )
                    )
                )
            }

        }
    }

    ActionPicker(
        expanded = isActionPickerExpanded,
        onDismissRequest = { isActionPickerExpanded = false },
        onUpdateMediasToSend = { mediasToSend.addOrNot(it) }
    )
    HorizontalFloatingToolbar(
        expanded = false,
        modifier = Modifier
            .padding(horizontal = 5.dp)
            .imePadding()
            .navigationBarsPadding(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column {
            LazyRow {
                items(
                    items = mediasToSend,
                    key = { it.path.toString() }
                ) { media ->
                    val mediaType = remember { context.contentResolver.getType(media) ?: "" }

                    if (mediaType.startsWith("video/") || mediaType.startsWith("image/")) {
                        MediaThumbnail(
                            modifier = Modifier.animateItem(),
                            media = media,
                            mediaType = mediaType,
                            onRemoveFromList = { mediasToSend.remove(media) }
                        )
                    } else {
                        GenericThumbnail(
                            modifier = Modifier.animateItem(),
                            mediaType = mediaType,
                            onRemoveFromList = { mediasToSend.remove(media) }
                        )
                    }

                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextField(
                    state = textFieldState,
                    placeholder = {
                        CuteText(
                            text = "Message",
                            color = MaterialTheme.colorScheme.onBackground.copy(0.85f)
                        )
                    },
                    colors = TextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                            0.5f
                        ),
                        focusedContainerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                            0.5f
                        ),
                        disabledIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    leadingIcon = {
                        IconButton(
                            onClick = { isActionPickerExpanded = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = null
                            )
                        }
                    },
                    trailingIcon = {
                        val defaultSim = cuteSimCards.fastFirst { it.subId == defaultSimCard }

                        IconButton(
                            onClick = { simSelectorVisible = true }
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SimCard,
                                contentDescription = null,
                                tint = Color(defaultSim.color)
                            )
                        }
                    },
                    shape = RoundedCornerShape(24.dp),
                    lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )
                IconButton(
                    onClick = {
                        onSendMessage(value)
                        value = ""
                    },
                    enabled = value.isNotEmpty() && value.isNotBlank()
                ) {
                    val tint by animateColorAsState(
                        targetValue = if (value.isNotEmpty() && value.isNotBlank()) LocalContentColor.current else MaterialTheme.colorScheme.surfaceVariant
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = null,
                        tint = tint
                    )
                }
            }
        }
    }

}