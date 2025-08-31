package com.sosauce.cuteconnect.data.conversation_settings

import com.sosauce.cuteconnect.domain.model.ConversationSettings

sealed class ConversationSettingActions {
    data class UpsertConversationSettings(val conversationSettings: ConversationSettings) : ConversationSettingActions()
}