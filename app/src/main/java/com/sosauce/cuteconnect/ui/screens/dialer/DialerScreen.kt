@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.dialer

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialShapes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.toShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import com.sosauce.cuteconnect.ui.shared_components.searchbars.CuteSearchbar
import com.sosauce.cuteconnect.utils.CuteRoundedCornerShape
import com.sosauce.cuteconnect.utils.groupSubsequentlyBy
import com.sosauce.cuteconnect.utils.selfAlignHorizontally
import com.sosauce.cuteconnect.utils.toReadableDate

@Composable
fun DialerScreen(
    state: DialerState,
    onNavigate: (Screen) -> Unit,
    onHandleCallActions: (CallAction) -> Unit
) {

    Scaffold(
        bottomBar = {
            CuteSearchbar(
                modifier = Modifier.selfAlignHorizontally(),
                sortingMenu = {},
                fab = {
                    FloatingActionButton(
                        onClick = { onNavigate(Screen.Dialpad) },
                        shape = MaterialShapes.Cookie9Sided.toShape()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.dialpad),
                            contentDescription = null
                        )
                    }
                },
                onNavigate = onNavigate
            )
        }
    ) { paddingValues ->
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

            state.callLogs
                .groupBy { it.date.toReadableDate() }
                .forEach { (date, callLog) ->

                    val groupedCalls = callLog.groupSubsequentlyBy { it.number }
                    item {
                        Text(
                            text = date,
                            style = MaterialTheme.typography.bodyLargeEmphasized.copy(
                                color = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                        )
                    }

                    itemsIndexed(
                        items = groupedCalls,
                        key = { _, (call, _) -> call.id }
                    ) { index, (callLog, appearances) ->
                        CallLogItem(
                            callLog = callLog,
                            numberOfAppearance = appearances,
                            onCallAction = onHandleCallActions,
                            shape = CuteRoundedCornerShape(
                                top = if (index == 0) 24.dp else 4.dp,
                                bottom = if (index == groupedCalls.lastIndex) 24.dp else 0.dp
                            ),
                            modifier = Modifier.animateItem()
                        )
                    }
                }
        }
    }

}