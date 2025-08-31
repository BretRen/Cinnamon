@file:OptIn(ExperimentalSharedTransitionApi::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3ExpressiveApi::class
)

package com.sosauce.cuteconnect.ui.shared_components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.utils.CurrentScreen
import com.sosauce.cuteconnect.utils.rememberSearchbarMaxFloatValue
import com.sosauce.cuteconnect.utils.rememberSearchbarRightPadding


@Composable
fun CuteSearchbar(
    modifier: Modifier = Modifier,
    query: String = "",
    onQueryChange: (String) -> Unit = {},
    trailingIcon: @Composable (() -> Unit)? = null,
    fab: @Composable (() -> Unit)? = null,
    onNavigate: (Screen) -> Unit,
) {

    val screenToLeadingIcon =
        mapOf(
            Screen.Messages to R.drawable.message_rounded,
            Screen.Contacts to R.drawable.contacts,
            Screen.Dialer to R.drawable.phone,
        )
    var isInScreenSelectionMode by remember { mutableStateOf(false) }


    Column(
        modifier = modifier
            .fillMaxWidth(rememberSearchbarMaxFloatValue())
            .padding(end = rememberSearchbarRightPadding())
            .navigationBarsPadding()
            .imePadding()
    ) {
        Row(
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .fillMaxWidth()
        ) { fab?.invoke() }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(6.dp)
        ) {
            AnimatedContent(
                targetState = isInScreenSelectionMode,
                transitionSpec = { scaleIn() togetherWith scaleOut() }
            ) {
                if (it) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceAround,
                        modifier = Modifier
                            .height(56.dp)
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
                                shape = RoundedCornerShape(50)
                            )
                    ) {
                        screenToLeadingIcon.onEachIndexed { index, (screen, icon) ->

                            val bgColor by animateColorAsState(
                                targetValue = if (CurrentScreen.screen == screen) MaterialTheme.colorScheme.primary else Color.Transparent
                            )
                            IconButton(
                                onClick = {
                                    onNavigate(screen)
                                    isInScreenSelectionMode = false
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = bgColor
                                )
                            ) {
                                Icon(
                                    painter = painterResource(icon),
                                    contentDescription = null
                                )
                            }
                        }
                    }
                } else {
                    SearchBarDefaults.InputField(
                        query = query,
                        onQueryChange = onQueryChange,
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        placeholder = {
                            CuteText(
                                text = "Search here",
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                                maxLines = 1
                            )
                        },
                        leadingIcon = {
                            var screenSelectionExpanded by remember { mutableStateOf(false) }

                            IconButton(
                                onClick = { isInScreenSelectionMode = true }
                            ) {
                                Icon(
                                    painter = painterResource(screenToLeadingIcon[CurrentScreen.screen] ?: R.drawable.message_rounded),
                                    contentDescription = null
                                )
                            }
                            ScreenSelection(
                                expanded = screenSelectionExpanded,
                                onDismissRequest = { screenSelectionExpanded = false },
                                onNavigate = onNavigate
                            )
                        },
                        trailingIcon = trailingIcon,
                        modifier = Modifier
                            .clip(RoundedCornerShape(50.dp))
                            .background(
                                color = MaterialTheme.colorScheme.secondaryContainer.copy(0.5f),
                                shape = RoundedCornerShape(50)
                            )
                    )
                }
            }
        }
    }
}






