package com.sosauce.cinnamon.data.contact_settings

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactSettingsDao {

    @Upsert
    suspend fun upsertContact(contactSettings: ContactSettings)

    @Query("SELECT * FROM contactsettings WHERE id = :contactId LIMIT 1")
    fun getContactSettings(contactId: Long): Flow<ContactSettings?>

    @Query("SELECT poster FROM contactsettings WHERE id = :contactId LIMIT 1")
    fun getContactPoster(contactId: Long): String?

}