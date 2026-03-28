package com.sosauce.cinnamon.data.conversation_settings

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.Upsert
import com.sosauce.cinnamon.domain.model.ConversationSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface ConversationSettingsDao {

    @Upsert
    suspend fun upsertConversation(conversationSettings: ConversationSettings)

    @Query("SELECT * FROM conversationsettings WHERE threadId = :threadId LIMIT 1")
    fun getConversationSettings(threadId: Long): Flow<ConversationSettings?>

    @Query("SELECT threadId, draft FROM conversationsettings")
    fun getAllDrafts(): Flow<Map<@MapColumn(columnName = "threadId") Long, @MapColumn(columnName = "draft") String>>

    @Query("SELECT draft FROM conversationsettings WHERE :threadId = threadId LIMIT 1")
    fun getDraftForThread(threadId: Long): String?

}