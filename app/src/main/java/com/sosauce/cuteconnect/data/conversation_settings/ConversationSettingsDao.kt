package com.sosauce.cuteconnect.data.conversation_settings

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.sosauce.cuteconnect.domain.model.ConversationSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationSettingsDao {

    @Upsert
    suspend fun upsertConversation(conversationSettings: ConversationSettings)

    @Query("SELECT * FROM conversationSettings WHERE convoId = :convoInt LIMIT 1")
    fun getConversationSettings(convoInt: Long): Flow<ConversationSettings?>

    @Query("SELECT convoId FROM conversationsettings WHERE isPinned = 1")
    fun getPinnedConversations(): Flow<List<Int>>

    @Query("SELECT draft FROM conversationsettings WHERE convoId = :threadId")
    fun getConversationDraft(threadId: Long): Flow<String?>
}