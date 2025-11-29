package com.sosauce.cuteconnect.ui.screens.archived

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sosauce.cuteconnect.ui.navigation.Screen
import com.sosauce.cuteconnect.ui.screens.messages.components.Conversation
import com.sosauce.cuteconnect.ui.shared_components.buttons.CuteNavigationButton

@Composable
fun ArchivedThreads(
    state: ArchivedState,
    onNavigate: (Screen) -> Unit,
    onNavigateUp: () -> Unit
) {
    Scaffold(
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                CuteNavigationButton(onNavigateUp = onNavigateUp)
            }
        }
    ) { paddingValues ->
        LazyColumn(
            contentPadding = paddingValues
        ) {
            items(
                items = state.threads,
                key = { it.threadId }
            ) { thread ->
                Conversation(
                    cuteConversation = thread,
                    cuteContact = null,
                    onClick = { onNavigate(Screen.Conversation(thread.threadId)) },
                    onLongClick = {}
                )
            }
        }

    }
}