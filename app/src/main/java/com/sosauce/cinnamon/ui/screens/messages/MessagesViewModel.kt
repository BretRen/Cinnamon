package com.sosauce.cinnamon.ui.screens.messages

import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cinnamon.data.conversation_settings.ConversationSettingsDao
import com.sosauce.cinnamon.data.datastore.UserPreferences
import com.sosauce.cinnamon.domain.model.CuteConversation
import com.sosauce.cinnamon.domain.repository.MessagesRepository
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
                conversationSettingsDao.getAllDrafts(),
                userPreferences.hasArchivedThreads
            ) { archived, pinned, threads, allDrafts, hasArchived ->
                val (pinnedThreads, unpinnedThreads) = threads
                    .fastMap {
                        val draft = allDrafts[it.threadId] ?: ""
                        it.copy(draft = draft)
                    }
                    .fastFilter { it.threadId.toString() !in archived }
                    .partition { it.threadId.toString() in pinned }

                MessagesState(
                    isLoading = false,
                    conversations = unpinnedThreads,
                    pinnedConversations = pinnedThreads,
                    hasArchivedThreads = hasArchived
                )

            }.collectLatest { state -> _state.update { state } }
        }
    }

    fun handleThreadsAction(action: ThreadsAction) {
        when(action) {
            is ThreadsAction.ArchiveThreads -> {
                viewModelScope.launch {
                    userPreferences.archiveThreads(action.threadIds)
                }
            }

            is ThreadsAction.PinThreads -> {
                viewModelScope.launch {
                    userPreferences.pinThreads(action.threadIds)
                }
            }

            is ThreadsAction.DeleteThreads -> {
                viewModelScope.launch {
                    messagesRepository.deleteThreads(action.threadIds)
                }
            }
        }

    }

}

data class MessagesState(
    val isLoading: Boolean = false,
    val hasArchivedThreads: Boolean = false,
    val conversations: List<CuteConversation> = emptyList(),
    val pinnedConversations: List<CuteConversation> = emptyList()
)

sealed interface ThreadsAction {
    data class ArchiveThreads(val threadIds: List<Long>) : ThreadsAction
    data class PinThreads(val threadIds: List<Long>) : ThreadsAction
    data class DeleteThreads(val threadIds: List<Long>) : ThreadsAction
}
