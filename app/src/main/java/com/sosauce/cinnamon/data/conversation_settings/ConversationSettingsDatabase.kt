package com.sosauce.cinnamon.data.conversation_settings

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sosauce.cinnamon.domain.model.ConversationSettings

@Database(
    entities = [ConversationSettings::class],
    version = 1
)
@TypeConverters(RoomConverters::class)
abstract class ConversationSettingsDatabase : RoomDatabase() {
    abstract val dao: ConversationSettingsDao
}