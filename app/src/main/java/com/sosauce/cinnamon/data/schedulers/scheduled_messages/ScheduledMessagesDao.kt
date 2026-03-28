package com.sosauce.cinnamon.data.schedulers.scheduled_messages

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledMessagesDao {


    @Upsert
    fun upsertScheduledMessage(scheduledMessage: ScheduledMessage): Long

    @Query("SELECT * FROM scheduledmessage WHERE :threadId = threadId")
    fun getScheduledMessagesForThread(threadId: Long): Flow<List<ScheduledMessage>>

    @Query("SELECT * FROM scheduledmessage WHERE :id = id")
    fun getScheduledMessageById(id: Long): ScheduledMessage

    @Delete
    fun deleteScheduledMessage(scheduledMessage: ScheduledMessage)
}