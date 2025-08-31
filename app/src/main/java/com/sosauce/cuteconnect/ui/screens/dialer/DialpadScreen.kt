package com.sosauce.cuteconnect.ui.screens.dialer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sosauce.cuteconnect.data.actions.CallAction
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.ContactListItem
import com.sosauce.cuteconnect.ui.screens.phone.components.DisableSoftKeyboard
import com.sosauce.cuteconnect.ui.shared_components.text.CuteText
import com.sosauce.cuteconnect.utils.rememberFocusRequester

@Composable
fun DialpadScreen(
    contacts: List<CuteContact>,
    onNavigate: (Screen) -> Unit,
    onHandleCallAction: (CallAction) -> Unit
) {
    var value by remember { mutableStateOf("") }
    val row1 = arrayOf("1", "2", "3")
    val row2 = arrayOf("4", "5", "6")
    val row3 = arrayOf("7", "8", "9")
    val row4 = arrayOf("*", "0", "#")
    val focusRequester = rememberFocusRequester()

    Scaffold(
        bottomBar = {
            Column(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.background)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    //verticalAlignment = Alignment.CenterVertically
                ) {
                    DisableSoftKeyboard {
                        OutlinedTextField(
                            value = value,
                            onValueChange = { value = it },
                            singleLine = true,
                            modifier = Modifier.focusRequester(focusRequester),
                        )
                    }
//                IconButton(
//                    onClick = {}
//                ) {
//                    Icon(
//                        imageVector = Icons.AutoMirrored.Rounded.Backspace,
//                        contentDescription = null
//                    )
//                }
                }
                Spacer(Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row1.forEach { number ->
                        TextButton(
                            onClick = { value += number },
                            modifier = Modifier.width(100.dp)
                        ) {
                            CuteText(
                                text = number,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row2.forEach { number ->
                        TextButton(
                            onClick = { value += number },
                            modifier = Modifier.width(100.dp)
                        ) {
                            CuteText(
                                text = number,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    row3.forEach { number ->
                        TextButton(
                            onClick = { value += number },
                            modifier = Modifier.width(100.dp)
                        ) {
                            CuteText(
                                text = number,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ){
                    row4.forEach { number ->
                        TextButton(
                            onClick = { value += number },
                            modifier = Modifier.width(100.dp)
                        ) {
                            CuteText(
                                text = number,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
                IconButton(
                    onClick = { onHandleCallAction(CallAction.LaunchCall(value)) },
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(50.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = null
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues
        ) {
            items(
                items = contacts,
                key = { it.id }
            ) { contact ->
                ContactListItem(
                    contact = contact,
                    onContactClick = { onNavigate(Screen.ContactDetails(contact.id)) }
                )
            }
        }

    }
}