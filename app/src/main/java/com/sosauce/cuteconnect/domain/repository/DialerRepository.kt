package com.sosauce.cuteconnect.domain.repository

import android.content.Context
import android.provider.CallLog
import androidx.compose.ui.util.fastMap
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.geocoding.PhoneNumberOfflineGeocoder
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteCallLog
import com.sosauce.cuteconnect.utils.beautifyNumber
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import com.sosauce.cuteconnect.utils.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import java.util.Locale

class DialerRepository(
    private val context: Context,
    private val phoneUtil: PhoneNumberUtil,
    private val geocodingUtil: PhoneNumberOfflineGeocoder
) {


    private val locale = Locale.getDefault()
    fun fetchLatestCallLog(): Flow<List<CuteCallLog>> {
        return context.contentResolver.observe(CallLog.Calls.CONTENT_URI).map {
            fetchCallLogs()
        }.flowOn(Dispatchers.IO)
    }


    private fun fetchCallLogs(): List<CuteCallLog> {

        data class Row(
            val id: Long,
            val number: String,
            val type: Int,
            val date: Long,
            val duration: Long
        )

        val rows = mutableListOf<Row>()


        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE, // Incoming, outgoing, missed
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION
        )

        context.contentResolver.query(
            CallLog.Calls.CONTENT_URI,
            projection,
            null,
            null,
            "${CallLog.Calls.DATE} DESC"
        )?.use { cursor ->

            val idColumn = cursor.getColumnIndexOrThrow(CallLog.Calls._ID)
            val numberColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val callTypeColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val dateColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val durationColumn = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)

            while (cursor.moveToNext()) {

                val id = cursor.getLong(idColumn)
                val number = cursor.getString(numberColumn).ifEmpty { context.getString(R.string.unknown) }
                val callType = cursor.getInt(callTypeColumn)
                val date = cursor.getLong(dateColumn)
                val duration = cursor.getLong(durationColumn)

                rows.add(
                    Row(
                        id = id,
                        number = number,
                        type = callType,
                        date = date,
                        duration = duration
                    )
                )

            }
        }

        if (rows.isEmpty()) return emptyList()

        val uniqueNumbers = rows.fastMap { it.number }.toSet()
        val countryToNumber = uniqueNumbers.associateWith { number ->
            try {
                val numberProto = phoneUtil.parse(number, locale.country)
                geocodingUtil.getDescriptionForNumber(numberProto, locale).ifEmpty { null }
            } catch (_: NumberParseException) {
                null
            }
        }
        // TODO: this is very slow
        val numberToContactNameOrBeautified = uniqueNumbers.associateWith { number ->
            number.getContactNameOrNothing(context).beautifyNumber()
        }

        return rows.fastMap { row ->

            val number = row.number

            CuteCallLog(
                id = row.id,
                rawNumber = number,
                beautifiedNumberOrName = numberToContactNameOrBeautified[number] ?: "",
                callType = row.type,
                date = row.date,
                duration = row.duration,
                country = countryToNumber[number]
            )
        }
    }

    fun deleteCallLog(id: Long) = context.contentResolver.delete(CallLog.Calls.CONTENT_URI, "${CallLog.Calls._ID} = ?", arrayOf(id.toString()))


}