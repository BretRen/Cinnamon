package com.sosauce.cuteconnect.ui.screens.messages

import android.app.Application
import android.net.Uri
import androidx.compose.ui.util.fastMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingActions
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingsDao
import com.sosauce.cuteconnect.data.managers.ActiveThreadId
import com.sosauce.cuteconnect.data.schedulers.SendMessageWorker
import com.sosauce.cuteconnect.data.schedulers.scheduled_messages.ScheduledMessage
import com.sosauce.cuteconnect.data.schedulers.scheduled_messages.ScheduledMessagesDao
import com.sosauce.cuteconnect.data.schedulers.scheduled_messages.toCuteMessage
import com.sosauce.cuteconnect.data.telephony.CuteTelephonyManager
import com.sosauce.cuteconnect.domain.model.ConversationSettings
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.domain.model.CuteSimCard
import com.sosauce.cuteconnect.domain.repository.ConversationsRepository
import com.sosauce.cuteconnect.domain.repository.SimsRepository
import com.sosauce.cuteconnect.utils.beautifyNumber
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.utils.toDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import java.util.concurrent.TimeUnit

class ConversationDetailsViewModel(
    private val application: Application,
    private val threadId: Long,
    private val conversationsRepository: ConversationsRepository,
    private val conversationSettingsDao: ConversationSettingsDao,
    private val cuteTelephonyManager: CuteTelephonyManager,
    private val scheduledMessagesDao: ScheduledMessagesDao,
    private val workManager: WorkManager
) : AndroidViewModel(application) {

    private val _state = MutableStateFlow(ConversationDetailsState(isLoading = true, threadId = threadId))
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            supervisorScope {
                launch {
                    combine(
                        conversationsRepository.fetchLatestSmsForThread(threadId),
                        conversationsRepository.fetchLatestMmsForThread(threadId),
                        scheduledMessagesDao.getScheduledMessagesForThread(threadId),
                    ) { sms, mms, scheduled -> sms + mms + scheduled.fastMap { it.toCuteMessage() } }.collectLatest { messages ->
                        _state.update {
                            it.copy(
                                messages = messages.sortedByDescending { it.date }.groupBy { it.date.toDate() },
                                isLoading = false
                            )
                        }
                    }
                }
                launch {
                    val threadRecipients = conversationsRepository.fetchThreadRecipients(threadId)

                    _state.update {
                        it.copy(
                            recipients = threadRecipients,
                            nameOrBeautifiedRecipients = threadRecipients.fastMap { it.getContactNameOrNothing(application).beautifyNumber() }
                        )
                    }
                }
                launch {
                    conversationSettingsDao.getConversationSettings(threadId).collectLatest { settings ->
                        _state.update {
                            it.copy(settings = settings ?: ConversationSettings(threadId = threadId))
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
                viewModelScope.launch {
                    cuteTelephonyManager.markConversationAsRead(threadId)
                }
            }
            is ConversationActions.SendMessage -> {
                viewModelScope.launch {
                    cuteTelephonyManager.sendMessage(
                        addresses = action.addresses,
                        message = action.message,
                        attachments = action.attachments
                    )
                }
            }

            is ConversationActions.ScheduleMessage -> {
                viewModelScope.launch(Dispatchers.IO) {
                    val scheduledMessageId = scheduledMessagesDao.upsertScheduledMessage(action.scheduledMessage)
                    val delay = action.scheduledMessage.sendAt - System.currentTimeMillis()
                    val request = OneTimeWorkRequestBuilder<SendMessageWorker>()
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .setInputData(
                            workDataOf(
                                SendMessageWorker.SCHEDULED_MESSAGE_ID to scheduledMessageId
                            )
                        )
                        .build()
                    workManager.enqueue(request)
                }

            }
        }
    }

}

data class ConversationDetailsState(
    val isLoading: Boolean = false,
    val threadId: Long = 0,
    val recipients: List<String> = emptyList(), // raw phone numbers in the conversation
    val nameOrBeautifiedRecipients: List<String> = emptyList(), // phone numbers in the conversation
    val settings: ConversationSettings = ConversationSettings(),
    val messages: Map<String, List<CuteMessage>> = emptyMap() // formatted date to messages
)

sealed class ConversationActions {
    data object MarkAsRead : ConversationActions()
    data class SendMessage(
        val addresses: List<String>,
        val message: String,
        val attachments: List<Uri>
    ) : ConversationActions()

    data class ScheduleMessage(
        val scheduledMessage: ScheduledMessage
    ) : ConversationActions()
}