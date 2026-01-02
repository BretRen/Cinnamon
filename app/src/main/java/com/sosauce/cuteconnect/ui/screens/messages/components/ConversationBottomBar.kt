@file:OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalHazeMaterialsApi::class)

package com.sosauce.cuteconnect.ui.screens.messages.components

import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.clearText
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingToolbarDefaults
import androidx.compose.material3.HorizontalFloatingToolbar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.util.fastFirstOrNull
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.datastore.rememberDefaultSimCard
import com.sosauce.cuteconnect.domain.model.CuteSimCard
import com.sosauce.cuteconnect.ui.shared_components.SimSelector
import com.sosauce.cuteconnect.utils.addOrNot
import com.sosauce.cuteconnect.utils.selfAlignHorizontally
import dev.chrisbanes.haze.materials.ExperimentalHazeMaterialsApi

@Composable
fun ConversationBottomBar(
    modifier: Modifier = Modifier,
    onSaveDraft: (String) -> Unit,
    onSendMessage: (message: String) -> Unit,
    cuteSimCards: List<CuteSimCard>,
) {

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
            if (textFieldState.text.isNotEmpty()) {
                onSaveDraft(textFieldState.text.toString())
            }

        }
    }

    ActionPicker(
        expanded = isActionPickerExpanded,
        onDismissRequest = { isActionPickerExpanded = false },
        onUpdateMediasToSend = { mediasToSend.addOrNot(it) }
    )


    HorizontalFloatingToolbar(
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
            .selfAlignHorizontally(),
        expanded = true,
//        floatingActionButton = {
//            FloatingToolbarDefaults.VibrantFloatingActionButton(
//                onClick = {
//                    onSendMessage(textFieldState.text.toString())
//                    textFieldState.clearText()
//                }
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.send_filled),
//                    contentDescription = null
//                )
//            }
//        }
    ) {
        Row(
            modifier = Modifier
                .animateContentSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {},
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    painter = painterResource(R.drawable.add),
                    contentDescription = null
                )
            }
            TextField(
                state = textFieldState,
                shape = FloatingToolbarDefaults.ContainerShape,
                lineLimits = TextFieldLineLimits.SingleLine,
                colors = TextFieldDefaults.colors(
                    unfocusedIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent
                ),
                trailingIcon = {
                    val defaultSim = cuteSimCards.fastFirstOrNull { it.subId == defaultSimCard }

                    IconButton(
                        onClick = { simSelectorVisible = true }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.sim_card_filled),
                            contentDescription = null,
                            tint = Color(defaultSim?.color ?: 0)
                        )
                    }
                }
            )
            AnimatedVisibility(
                visible = textFieldState.text.isNotEmpty() && textFieldState.text.isNotBlank(),
                enter = slideInHorizontally { -it },
                exit = slideOutHorizontally { it }
            ) {
                IconButton(
                    onClick = {
                        onSendMessage(textFieldState.text.toString())
                        textFieldState.clearText()
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.send_filled),
                        contentDescription = null
                    )
                }
            }
        }
    }

//    ToolbarSkeleton(
//        modifier = Modifier.imePadding()
//    ) {
//        Row(
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//
//            TextField(
//                state = textFieldState,
//                placeholder = {
//                    Text(
//                        text = "Message",
//                        color = MaterialTheme.colorScheme.onSurfaceVariant
//                    )
//                },
//                colors = TextFieldDefaults.colors(
//                    disabledIndicatorColor = Color.Transparent,
//                    focusedIndicatorColor = Color.Transparent,
//                    unfocusedIndicatorColor = Color.Transparent
//                ),
//                leadingIcon = {
//                    IconButton(
//                        onClick = { isActionPickerExpanded = true }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.add),
//                            contentDescription = null
//                        )
//                    }
//                },
//                trailingIcon = {
//                    val defaultSim = cuteSimCards.fastFirstOrNull { it.subId == defaultSimCard }
//
//                    IconButton(
//                        onClick = { simSelectorVisible = true }
//                    ) {
//                        Icon(
//                            painter = painterResource(R.drawable.sim_card_filled),
//                            contentDescription = null,
//                            tint = Color(defaultSim?.color ?: 0)
//                        )
//                    }
//                },
//                shape = FloatingToolbarDefaults.ContainerShape,
//                lineLimits = TextFieldLineLimits.MultiLine(maxHeightInLines = 4),
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//
//            )
//            IconButton(
//                onClick = {
//                    onSendMessage(textFieldState.text.toString())
//                    textFieldState.clearText()
//                },
//                enabled = textFieldState.text.isNotEmpty() && textFieldState.text.isNotBlank()
//            ) {
//                Icon(
//                    painter = painterResource(R.drawable.send_filled),
//                    contentDescription = null
//                )
//            }
//        }
//    }
}