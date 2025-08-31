package com.sosauce.cuteconnect.data.datastore

import android.content.Context
import android.telephony.SubscriptionManager
import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.BLOCKED_NUMBERS
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.DEFAULT_SIM
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.MMS_MAX_SIZE_LIMIT
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.PINNED_CONVOS
import com.sosauce.cuteconnect.utils.MmsSize

private const val PREFERENCES_NAME = "settings"

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(PREFERENCES_NAME)

data object PreferencesKeys {
    val PINNED_CONVOS = stringSetPreferencesKey("pinned_convos")
    val MMS_MAX_SIZE_LIMIT = longPreferencesKey("MMS_MAX_SIZE_LIMIT")
    val BLOCKED_NUMBERS = stringSetPreferencesKey("BLOCKED_NUMBERS")
    val DEFAULT_SIM = intPreferencesKey("DEFAULT_SIM")
}


//@Composable
//fun rememberPinnedConversations() =
//    rememberPreference(key = PINNED_CONVOS, defaultValue = emptySet())

@Composable
fun rememberMmsMaxSizeLimit() = rememberPreference(MMS_MAX_SIZE_LIMIT, MmsSize.FILE_SIZE_600_KB)

@Composable
fun rememberBlockedNumbers() = rememberPreference(BLOCKED_NUMBERS, emptySet())

@Composable
fun rememberDefaultSimCard() = rememberPreference(DEFAULT_SIM, SubscriptionManager.getDefaultSubscriptionId())

fun getBlockedNumber(context: Context) = getPreference(BLOCKED_NUMBERS, emptySet(), context)

suspend fun saveBlockedNumber(
    blockedNumbers: Set<String>,
    context: Context
) {
    savePreference(
        key = BLOCKED_NUMBERS,
        newValue = blockedNumbers,
        context = context
    )
}