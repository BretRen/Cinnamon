package com.sosauce.cuteconnect.domain.repository

import android.content.Context
import com.sosauce.cuteconnect.data.datastore.PreferencesKeys.BLOCKED_NUMBERS
import com.sosauce.cuteconnect.data.datastore.UserPreferences
import com.sosauce.cuteconnect.data.datastore.dataStore
import com.sosauce.cuteconnect.utils.copyMutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

class BlockedNumbersManager(private val userPreferences: UserPreferences) {

    /**
     * Strips a number to only keep numbers, i.e +33 7 00 00 00 00 will give 33700000000. Country code is preserved.
     */
    private fun stripNumber(number: String): String {
        return buildString {
            for (char in number) {
                if (char == '+') append(char)
                if (char.isDigit()) append(char)
            }
        }
    }

    fun isNumberBlocked(number: String): Boolean {
        val normalizedNumber = stripNumber(number)

        return runBlocking { normalizedNumber in userPreferences.blockedNumbers.first() }
    }

    suspend fun blockNumber(number: String, context: Context) {
        val normalizedNumber = stripNumber(number)

        userPreferences.saveBlockedNumbers(userPreferences.blockedNumbers.first().copyMutate { add(normalizedNumber) })
    }

}