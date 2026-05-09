@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cinnamon.presentation.screens.dialer

import android.provider.CallLog
import android.telecom.Call
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastMap
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.google.common.collect.Multimaps.index
import com.sosauce.cinnamon.R
import com.sosauce.cinnamon.data.datastore.rememberGroupSubsequentCalls
import com.sosauce.cinnamon.data.datastore.rememberSortLogsAscending
import com.sosauce.cinnamon.domain.model.CuteCallLog
import com.sosauce.cinnamon.domain.model.CuteContact
import com.sosauce.cinnamon.presentation.navigation.Screen
import com.sosauce.cinnamon.presentation.screens.messages.components.bottombar.MoreOptions
import com.sosauce.cinnamon.presentation.screens.phone.CallAction
import com.sosauce.cinnamon.presentation.shared_components.NoSearchFound
import com.sosauce.cinnamon.presentation.shared_components.NoXFound
import com.sosauce.cinnamon.presentation.shared_components.SelectedBarSurface
import com.sosauce.cinnamon.presentation.shared_components.animations.AnimatedFab
import com.sosauce.cinnamon.presentation.shared_components.menus.SortingDropdownMenu
import com.sosauce.cinnamon.presentation.shared_components.searchbars.CuteSearchbar
import com.sosauce.cinnamon.utils.CuteRoundedCornerShape
import com.sosauce.cinnamon.utils.getItemShape
import com.sosauce.cinnamon.utils.groupSubsequentlyBy
import com.sosauce.cinnamon.utils.rememberInteractionSource
import com.sosauce.cinnamon.utils.selfAlignHorizontally
import com.sosauce.cinnamon.utils.toDate
import com.sosauce.sweetselect.rememberSweetSelectState
import kotlin.math.log

@Composable
fun DialerScreen(
    state: DialerState,
    onNavigate: (Screen) -> Unit,
    onHandleCallActions: (CallAction) -> Unit,
    onHandleDialerActions: (DialerAction) -> Unit
) {
    val sweetSelectState = rememberSweetSelectState<CuteCallLog>()
    val groupSubCalls by rememberGroupSubsequentCalls()
    var sortLogsAscending by rememberSortLogsAscending()

    Scaffold(
        bottomBar = {

            AnimatedContent(
                targetState = sweetSelectState.isInSelectionMode
            ) {
                if (it) {
                    val items = state.callLogs.values.flatten()

                    SelectedBarSurface(
                        modifier = Modifier.selfAlignHorizontally(),
                        items = items,
                        multiSelectState = sweetSelectState
                    ) {
                        ButtonGroup(
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Button(
                                onClick = {
                                    val logs = sweetSelectState.selectedItems.map { it.id }
                                    onHandleDialerActions(DialerAction.DeleteLogs(logs))
                                    sweetSelectState.clearSelected()
                                },
                                shape = RoundedCornerShape(topStart = 50.dp, bottomStart = 50.dp, topEnd = 50.dp, bottomEnd = 50.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                                    contentColor = contentColorFor(MaterialTheme.colorScheme.surfaceContainer)
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.delete_filled),
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                } else {
                    CuteSearchbar(
                        modifier = Modifier.selfAlignHorizontally(),
                        textFieldState = state.textFieldState,
                        sortingMenu = {
                            SortingDropdownMenu(
                                isSortedAscending = sortLogsAscending,
                                onChangeSorting = { sortLogsAscending = it }
                            ) {
                                repeat(5) { index ->

                                    val filter = when(index) {
                                        0 -> CallLogsFilter.ALL
                                        1 -> CallLogsFilter.CONTACTS
                                        2 -> CallLogsFilter.INCOMING
                                        3 -> CallLogsFilter.OUTGOING
                                        4 -> CallLogsFilter.MISSED
                                        else -> throw IndexOutOfBoundsException()
                                    }
                                    val text = when(index) {
                                        0 -> R.string.all
                                        1 -> R.string.contacts
                                        2 -> R.string.incoming
                                        3 -> R.string.outgoing
                                        4 -> R.string.missed
                                        else -> throw IndexOutOfBoundsException()
                                    }

                                    DropdownMenuItem(
                                        selected = filter == state.filter,
                                        onClick = { onHandleDialerActions(DialerAction.ChangeFilter(filter)) },
                                        shapes = MenuDefaults.itemShapes(),
                                        text = {
                                            Text(
                                                text = stringResource(text)
                                            )
                                        }
                                    )
                                }
                            }
                        },
                        fab = {
                            AnimatedFab(
                                onClick = { onNavigate(Screen.Dialpad()) },
                                icon = R.drawable.dialpad
                            )
                        },
                        onNavigate = onNavigate
                    )

                }
            }
        }
    ) { paddingValues ->

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                ContainedLoadingIndicator()
            }
        } else {
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
                                painter = painterResource(R.drawable.voicemail),
                                contentDescription = null
                            )
                            Spacer(Modifier.width(10.dp))
                            Text(stringResource(R.string.voicemail))
                        }
                    }
                    Spacer(Modifier.height(10.dp))
                }

                if (state.callLogs.isEmpty() && !state.isSearching) {
                    item {
                        NoXFound(
                            headlineText = R.string.no_calls_found,
                            bodyText = R.string.no_calls_found_desc,
                            icon = R.drawable.call_log_rounded
                        )
                    }
                } else {
                    if (state.callLogs.isNotEmpty()) {
                        if (groupSubCalls) {
                            state.callLogs
                                .forEach { (date, callLogs) ->

                                    val groupedCalls = callLogs.groupSubsequentlyBy { it.rawNumber }
                                    item(
                                        key = date
                                    ) {
                                        Text(
                                            text = date,
                                            style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                                                color = MaterialTheme.colorScheme.primary
                                            ),
                                            modifier = Modifier
                                                .animateItem()
                                                .padding(horizontal = 20.dp, vertical = 10.dp)
                                        )
                                    }

                                    items(
                                        items = groupedCalls,
                                        key = { (log, _) -> log.id }
                                    ) { (callLog, appearances) ->

                                        val isSelected by sweetSelectState.isSelectedAsState(callLog)


                                        CallLogItem(
                                            callLog = callLog,
                                            numberOfAppearance = appearances,
                                            isSelected = isSelected,
                                            onCallAction = onHandleCallActions,
                                            onNavigate = onNavigate,
                                            onDeleteCallLog = { onHandleDialerActions(DialerAction.DeleteLogs(listOf(callLog.id))) },
                                            modifier = Modifier.animateItem(),
                                            onClick = {
                                                if (sweetSelectState.isInSelectionMode) {
                                                    sweetSelectState.toggle(callLog)
                                                } else {
                                                    if (callLog.presentation == CallLog.Calls.PRESENTATION_ALLOWED) {
                                                        onHandleCallActions(CallAction.LaunchCall(callLog.rawNumber))
                                                    }
                                                }
                                            },
                                            onLongClick = { sweetSelectState.toggle(callLog) }
                                        )
                                    }
                                }
                        } else {
                            state.callLogs.forEach { (date, logs) ->
                                item(
                                    key = date
                                ) {
                                    Text(
                                        text = date,
                                        style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                                            color = MaterialTheme.colorScheme.primary
                                        ),
                                        modifier = Modifier
                                            .animateItem()
                                            .padding(horizontal = 20.dp, vertical = 10.dp)
                                    )
                                }
                                items(
                                    items = logs,
                                    key = { it.id }
                                ) { callLog ->

                                    val isSelected by sweetSelectState.isSelectedAsState(callLog)

                                    CallLogItem(
                                        modifier = Modifier.animateItem(),
                                        callLog = callLog,
                                        isSelected = isSelected,
                                        numberOfAppearance = 1,
                                        onCallAction = onHandleCallActions,
                                        onNavigate = onNavigate,
                                        onDeleteCallLog = { onHandleDialerActions(DialerAction.DeleteLogs(listOf(callLog.id))) },
                                        onClick = {
                                            if (sweetSelectState.isInSelectionMode) {
                                                sweetSelectState.toggle(callLog)
                                            } else {
                                                if (callLog.presentation == CallLog.Calls.PRESENTATION_ALLOWED) {
                                                    onHandleCallActions(CallAction.LaunchCall(callLog.rawNumber))
                                                }
                                            }
                                        },
                                        onLongClick = { sweetSelectState.toggle(callLog) }

                                    )
                                }
                            }
                        }
                    } else {
                        item { NoSearchFound() }
                    }
                }




            }
        }

    }

}