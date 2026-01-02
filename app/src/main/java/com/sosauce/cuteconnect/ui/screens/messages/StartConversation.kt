@file:OptIn(ExperimentalMaterial3Api::class)

package com.sosauce.cuteconnect.ui.screens.messages

import android.provider.Telephony
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastFilter
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.ContactListItem
import com.sosauce.cuteconnect.ui.shared_components.searchbars.MiniCuteSearchbar

@Composable
fun StartConversation(
    contacts: List<CuteContact>,
    onNavigateUp: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val textState = rememberTextFieldState()

    Scaffold(
        bottomBar = {
            MiniCuteSearchbar(
                modifier = Modifier.fillMaxWidth().wrapContentWidth(),
                textFieldState = textState,
                onNavigateUp = onNavigateUp
            )
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues
        ) {
            items(
                items = contacts.fastFilter { it.name.lowercase().contains(textState.text.toString().lowercase()) || it.phoneNumbers.firstOrNull()?.number?.contains(textState.text) == true },
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
                    onContactClick = { onNavigate(Screen.Conversation(Telephony.Threads.getOrCreateThreadId(context, contact.phoneNumbers.first().number))) },
                    showNumber = true
                )
            }
        }
    }
}