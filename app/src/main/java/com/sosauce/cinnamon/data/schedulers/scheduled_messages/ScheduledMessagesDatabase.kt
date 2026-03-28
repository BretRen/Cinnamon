package com.sosauce.cinnamon.data.schedulers.scheduled_messages

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [ScheduledMessage::class],
    version = 1
)
abstract class ScheduledMessagesDatabase : RoomDatabase() {
    abstract val dao: ScheduledMessagesDao
}