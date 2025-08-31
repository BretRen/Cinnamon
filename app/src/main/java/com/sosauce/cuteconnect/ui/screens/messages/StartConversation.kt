@file:OptIn(ExperimentalMaterial3Api::class)

package com.sosauce.cuteconnect.ui.screens.messages

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.ContactListItem
import com.sosauce.cuteconnect.ui.shared_components.MiniCuteSearchbar
import com.sosauce.cuteconnect.utils.getThreadIdOrCreate
import com.sosauce.cuteconnect.utils.rememberSearchbarAlignment
import com.sosauce.cuteconnect.utils.rememberSearchbarMaxFloatValue
import com.sosauce.cuteconnect.utils.rememberSearchbarRightPadding
import com.sosauce.cuteconnect.utils.showCuteSearchbar

@Composable
fun StartConversation(
    contacts: List<CuteContact>,
    onNavigateUp: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    var query by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        Scaffold { paddingValues ->
            LazyColumn(
                contentPadding = paddingValues,
                state = listState,
                modifier = Modifier.fillMaxSize()
            ) {
                items(
                    items = contacts.filter { it.name.lowercase().contains(query.lowercase()) || it.phoneNumbers.firstOrNull()?.number?.contains(query) == true },
                    key = { it.id }
                ) { contact ->
                    ContactListItem(
                        modifier = Modifier
                            .animateItem()
                            .padding(
                                vertical = 2.dp,
                                horizontal = 4.dp
                            ),
                        contact = contact,
                        onContactClick = { onNavigate(Screen.Conversation(contact.phoneNumbers.first().number.getThreadIdOrCreate(context))) },
                        showNumber = true
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = listState.showCuteSearchbar,
            enter = slideInVertically { it },
            exit = slideOutVertically { it },
            modifier = Modifier
                .navigationBarsPadding()
                .align(rememberSearchbarAlignment())
                .fillMaxWidth(rememberSearchbarMaxFloatValue())
                .padding(end = rememberSearchbarRightPadding())
                .imePadding()
        ) {
            MiniCuteSearchbar(
                query = query,
                onQueryChange = { query = it },
                onNavigateUp = onNavigateUp
            )
        }
    }
}