package com.sosauce.cuteconnect.data.datastore

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.datastore.preferences.core.edit
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.ARCHIVED_CONVOS
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.BLOCKED_NUMBERS
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.DEFAULT_MESSAGES_SIM
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.ENABLE_DELIVERY_REPORTS
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.PINNED_CONVOS
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.SEND_GROUP_AS_MMS
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.SEND_LONG_AS_MMS
import kotlinx.coroutines.flow.map

class UserPreferences(private val context: Context) {

    val pinnedConversations = context.dataStore.data.map {
        it[PINNED_CONVOS] ?: emptySet()
    }

    val archivedConversations = context.dataStore.data.map {
        it[ARCHIVED_CONVOS] ?: emptySet()
    }

    val blockedNumbers = context.dataStore.data.map {
        it[BLOCKED_NUMBERS] ?: emptySet()
    }

    val groupAsMms = context.dataStore.data.map {
        it[SEND_GROUP_AS_MMS] ?: false
    }

    val longAsMms = context.dataStore.data.map {
        it[SEND_LONG_AS_MMS] ?: false
    }

    val enableDeliveryReports = context.dataStore.data.map {
        it[ENABLE_DELIVERY_REPORTS] ?: false
    }

    suspend fun saveBlockedNumbers(blockedNumbers: Set<String>) {
        context.dataStore.edit {
            it[BLOCKED_NUMBERS] = blockedNumbers
        }
    }

    val defaultMessagesSim = context.dataStore.data.map {
        it[DEFAULT_MESSAGES_SIM] ?: SubscriptionManager.getDefaultSmsSubscriptionId()
    }

}