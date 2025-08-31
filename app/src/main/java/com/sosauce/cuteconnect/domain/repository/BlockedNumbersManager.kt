package com.sosauce.cuteconnect.domain.repository

import android.content.Context
import com.sosauce.cuteconnect.data.datastore.getBlockedNumber
import com.sosauce.cuteconnect.data.datastore.saveBlockedNumber
import com.sosauce.cuteconnect.utils.copyMutate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

object BlockedNumbersManager {

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

    fun isNumberBlocked(number: String, context: Context): Boolean {
        val allBlockedNumbers = runBlocking(Dispatchers.IO) { getBlockedNumber(context).first() }
        val normalizedNumber = stripNumber(number)

        return normalizedNumber in allBlockedNumbers
    }

    suspend fun blockNumber(number: String, context: Context) {
        val allBlockedNumbers = runBlocking(Dispatchers.IO) { getBlockedNumber(context).first() }
        val normalizedNumber = stripNumber(number)

        saveBlockedNumber(
            blockedNumbers = allBlockedNumbers.copyMutate { add(normalizedNumber) },
            context = context
        )
    }

}