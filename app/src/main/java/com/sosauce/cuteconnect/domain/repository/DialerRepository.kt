package com.sosauce.cuteconnect.domain.repository

import android.content.Context
import android.provider.CallLog
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteCallLog
import com.sosauce.cuteconnect.utils.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map

class DialerRepository(
    private val context: Context
) {

    fun fetchLatestCallLog(): Flow<List<CuteCallLog>> {
        return context.contentResolver.observe(CallLog.Calls.CONTENT_URI).map {
            fetchCallLogs()
        }.flowOn(Dispatchers.IO)
    }


    private fun fetchCallLogs(): List<CuteCallLog> {

        val callLogs = mutableListOf<CuteCallLog>()


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
                callLogs.add(
                    CuteCallLog(
                        id = cursor.getLong(idColumn),
                        number = cursor.getString(numberColumn).ifEmpty { context.getString(R.string.unknown) },
                        callType = cursor.getInt(callTypeColumn),
                        date = cursor.getLong(dateColumn),
                        duration = cursor.getLong(durationColumn)
                    )
                )
            }
        }

        return callLogs
    }

}