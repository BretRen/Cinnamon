package com.sosauce.cuteconnect.ui.screens.messages

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingActions
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingsDao
import com.sosauce.cuteconnect.data.telephony.CuteTelephonyManager
import com.sosauce.cuteconnect.domain.model.ConversationSettings
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.domain.model.CuteSimCard
import com.sosauce.cuteconnect.domain.repository.ConversationsRepository
import com.sosauce.cuteconnect.domain.repository.SimsRepository
import com.sosauce.cuteconnect.utils.toReadableDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

class ConversationDetailsViewModel(
    private val threadId: Long,
    private val conversationsRepository: ConversationsRepository,
    private val simsRepository: SimsRepository,
    private val conversationSettingsDao: ConversationSettingsDao,
    private val cuteTelephonyManager: CuteTelephonyManager
) : ViewModel() {

    private val _state = MutableStateFlow(ConversationDetailsState(isLoading = true, threadId = threadId))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                launch {
                    conversationsRepository.fetchLatestMessagesForThread(threadId).collectLatest { messages ->
                        _state.update {
                            it.copy(
                                messages = messages.groupBy { message -> message.date.toReadableDate() },
                                isLoading = false
                            )
                        }
                    }
                }
                launch {
                    val sims = simsRepository.fetchSims()
                    _state.update {
                        it.copy(
                            simCards = sims
                        )
                    }
                }
                launch {
                    _state.update {
                        it.copy(
                            recipients = conversationsRepository.fetchThreadRecipients(threadId)
                        )
                    }
                }
                launch {
                    conversationSettingsDao.getConversationSettings(threadId).collectLatest { settings ->
                        _state.update {
                            it.copy(settings = settings ?: ConversationSettings(threadId))
                        }
                    }
                }
            }

        }
    }

    fun deleteConversation() = viewModelScope.launch(Dispatchers.IO) { conversationsRepository.deleteConversation(threadId) }

    fun handleConversationSettingsActions(action: ConversationSettingActions) {
        when(action) {
            is ConversationSettingActions.UpsertConversationSettings -> {
                viewModelScope.launch(Dispatchers.IO) {
                    conversationSettingsDao.upsertConversation(action.conversationSettings)
                }
            }
        }
    }

    fun handleConversationActions(action: ConversationActions) {
        when(action) {
            is ConversationActions.MarkAsRead -> {
                viewModelScope.launch(Dispatchers.IO) {
                    cuteTelephonyManager.markAsRead(action.messageId)
                }
            }
            is ConversationActions.SendSms -> {
                viewModelScope.launch(Dispatchers.IO) {
                    cuteTelephonyManager.sendSms(
                        address = action.address,
                        message = action.message
                    )
                }
            }
        }
    }

}

data class ConversationDetailsState(
    val isLoading: Boolean = false,
    val threadId: Long = 0,
    val recipients: List<String> = emptyList(), // phone numbers in the conversation
    val contactPfp: Uri = Uri.EMPTY,
    val settings: ConversationSettings = ConversationSettings(),
    val messages: Map<String, List<CuteMessage>> = emptyMap(), // formatted date to messages
    val simCards: List<CuteSimCard> = emptyList()
)

sealed class ConversationActions {
    data class MarkAsRead(val messageId: Long) : ConversationActions()
    data class SendSms(
        val address: String,
        val message: String
    ) : ConversationActions()
}