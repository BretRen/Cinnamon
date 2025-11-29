@file:OptIn(ExperimentalMaterial3Api::class)

package com.sosauce.cuteconnect.ui.screens.messages

import android.provider.Telephony
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.input.rememberTextFieldState
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
import androidx.compose.ui.util.fastFilter
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.contacts.ContactListItem
import com.sosauce.cuteconnect.ui.shared_components.searchbars.MiniCuteSearchbar
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