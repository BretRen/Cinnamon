@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.dialer

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowOutward
import androidx.compose.material.icons.rounded.Dialpad
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Voicemail
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.R

import com.sosauce.cuteconnect.domain.model.CuteCallLog
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.shared_components.searchbars.CuteSearchbar
import androidx.compose.material3.Text
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import com.sosauce.cuteconnect.utils.groupSubsequentlyBy
import com.sosauce.cuteconnect.utils.rememberSearchbarAlignment
import com.sosauce.cuteconnect.utils.showCuteSearchbar
import com.sosauce.cuteconnect.utils.toReadableDate

@Composable
fun DialerScreen(
    state: DialerState,
    onNavigate: (Screen) -> Unit,
    onHandleCallActions: (CallAction) -> Unit
) {

    val listState = rememberLazyListState()

    Scaffold { paddingValues ->
        Box {
            LazyColumn(
                contentPadding = paddingValues
            ) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceContainer),
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(),
                        onClick = { onNavigate(Screen.Voicemail) }
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Voicemail,
                                contentDescription = null
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.voicemail))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                state.callLogs
                    .groupBy { it.date.toReadableDate() }
                    .forEach { (date, callLog) ->
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = date,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }

                        items(
                            items = callLog.groupSubsequentlyBy { it.number },
                            key = { (call, _) -> call.id }
                        ) { (callLog, appearances) ->
                            CallLogItem(
                                callLog = callLog,
                                numberOfAppearance = appearances,
                                onCallAction = onHandleCallActions,
                                modifier = Modifier
                                    .animateItem()
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable { onHandleCallActions(CallAction.LaunchCall(callLog.number)) }
                            )
                        }
                    }
            }

            AnimatedVisibility(
                visible = listState.showCuteSearchbar,
                modifier = Modifier.align(rememberSearchbarAlignment()),
                enter = slideInVertically { it },
                exit = slideOutVertically { it }
            ) {
                CuteSearchbar(
                    sortingMenu = {},
                    fab = {
                        FloatingActionButton(
                            onClick = { onNavigate(Screen.Dialpad) },
                            shape = MaterialShapes.Cookie9Sided.toShape()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Dialpad,
                                contentDescription = null
                            )
                        }
                    },
                    onNavigate = onNavigate
                )

            }


        }
    }

}