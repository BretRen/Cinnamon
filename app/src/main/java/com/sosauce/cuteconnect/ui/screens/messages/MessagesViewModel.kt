package com.sosauce.cuteconnect.ui.screens.messages

import android.app.Application
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingsDao
import com.sosauce.cuteconnect.data.datastore.UserPreferences
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.domain.repository.MessagesRepository
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class MessagesViewModel(
    private val messagesRepository: MessagesRepository,
    private val userPreferences: UserPreferences,
    private val conversationSettingsDao: ConversationSettingsDao
): ViewModel() {

    private val _state = MutableStateFlow(MessagesState(isLoading = true))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            combine(
                userPreferences.archivedConversations,
                userPreferences.pinnedConversations,
                messagesRepository.fetchLatestConversations(),
                conversationSettingsDao.getAllDrafts()
            ) { archived, pinned, threads, allDrafts ->
                threads
                    .fastMap {
                        val draft = allDrafts[it.threadId] ?: ""
                        it.copy(draft = draft)
                    }
                    .fastFilter { it.threadId.toString() !in archived }
                    .partition { it.threadId.toString() in pinned }
            }.collectLatest { (pinned, unpinned) ->
                _state.update {
                    it.copy(
                        isLoading = false,
                        conversations = unpinned,
                        pinnedConversations = pinned,
                    )
                }
            }
        }
    }

    fun deleteConversation(threadId: Long) = viewModelScope.launch {
        messagesRepository.deleteConversation(threadId)
    }

}

data class MessagesState(
    val isLoading: Boolean = false,
    val conversations: List<CuteConversation> = emptyList(),
    val pinnedConversations: List<CuteConversation> = emptyList()
)