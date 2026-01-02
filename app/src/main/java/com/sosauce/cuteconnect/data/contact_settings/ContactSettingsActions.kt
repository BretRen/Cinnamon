package com.sosauce.cuteconnect.data.contact_settings

sealed class ContactSettingsActions {
    data class UpsertContactSettings(val contactSettings: ContactSettings) : ContactSettingsActions()
}