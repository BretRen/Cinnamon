@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.sosauce.cuteconnect.ui.screens.dialer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastFirst
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.groupedContactsList
import com.sosauce.cuteconnect.ui.screens.phone.CallAction
import com.sosauce.cuteconnect.ui.screens.phone.components.DisableSoftKeyboard
import com.sosauce.cuteconnect.utils.rememberFocusRequester

@Composable
fun DialpadScreen(
    state: DialpadState,
    onNavigate: (Screen) -> Unit,
    onNavigateUp: () -> Unit,
    onHandleCallAction: (CallAction) -> Unit
) {

    if (state.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            ContainedLoadingIndicator()
        }
    } else {
        var value by remember { mutableStateOf("") }
        val dialpadLayout = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("*", "0", "#")
        )
        val focusRequester = rememberFocusRequester()
        var showMultiNumberSelection by remember { mutableStateOf(Pair(false, 0L)) }

        if (showMultiNumberSelection.first) {
            val contact = state.contacts.fastFirst { it.id == showMultiNumberSelection.second }

            AlertDialog(
                onDismissRequest = { showMultiNumberSelection = Pair(false, 0L) },
                icon = {
                    Icon(
                        painter = painterResource(R.drawable.call),
                        contentDescription = null
                    )
                },
                title = {
                    Text("Select a number to call")
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(
                        onClick = { showMultiNumberSelection = Pair(false, 0L) }
                    ) {
                        Text(stringResource(R.string.cancel))
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        contact.phoneNumbers.fastForEachIndexed { index, number ->
                            DropdownMenuItem(
                                shape = when(index) {
                                    0 -> MenuDefaults.leadingItemShape
                                    contact.phoneNumbers.lastIndex -> MenuDefaults.trailingItemShape
                                    else -> MenuDefaults.middleItemShape
                                },
                                onClick = { onHandleCallAction(CallAction.LaunchCall(number.number)) },
                                leadingIcon = { Text("${index + 1}.") },
                                text = { Text(number.number) }
                            )
                        }
                    }
                }

            )
        }


        Scaffold(
            modifier = Modifier.padding(horizontal = 10.dp),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.background)
                        .navigationBarsPadding()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        //verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onNavigateUp,
                            shapes = IconButtonDefaults.shapes()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.back),
                                contentDescription = null
                            )
                        }
                        DisableSoftKeyboard {
                            OutlinedTextField(
                                value = value,
                                onValueChange = { value = it },
                                singleLine = true,
                                modifier = Modifier.focusRequester(focusRequester),
                                colors = TextFieldDefaults.colors(
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedContainerColor = Color.Transparent
                                )
                            )
                        }
                        IconButton(
                            onClick = { value = value.dropLast(1) },
                            shapes = IconButtonDefaults.shapes(),
                            enabled = value.isNotEmpty()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.backspace),
                                contentDescription = null
                            )
                        }
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        dialpadLayout.fastForEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                rowKeys.fastForEach { number ->
                                    ElevatedButton(
                                        onClick = { value += number },
                                        modifier = Modifier.weight(1f),
                                        shapes = ButtonDefaults.shapes()
                                    ) {
                                        Text(
                                            text = number,
                                            style = MaterialTheme.typography.bodyLargeEmphasized
                                        )
                                    }
                                }
                            }
                        }
                        FilledIconButton(
                            onClick = { onHandleCallAction(CallAction.LaunchCall(value)) },
                            modifier = Modifier
                                .fillMaxWidth(0.5f)
                                .align(Alignment.CenterHorizontally)
                                .size(IconButtonDefaults.mediumContainerSize(IconButtonDefaults.IconButtonWidthOption.Wide)),
                            enabled = value.isNotEmpty() || value.isNotBlank()
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.call),
                                contentDescription = null
                            )
                        }
                    }
                }
            }
        ) { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues
            ) {
                groupedContactsList(
                    groupedContacts = state.contacts.fastFilter { it.phoneNumbers.fastAny { number -> number.number.contains(value) } }.groupBy { it.firstName.first() },
                    onContactClicked = { contact ->
                        if (contact.phoneNumbers.size > 1) {
                            showMultiNumberSelection = Pair(true, contact.id)
                        } else {
                            onHandleCallAction(CallAction.LaunchCall(contact.phoneNumbers.first().number))
                        }
                    }
                )
            }

        }

    }

}