package com.sosauce.cinnamon.domain.repository

import android.content.ContentUris
import android.content.Context
import android.provider.VoicemailContract
import com.sosauce.cinnamon.domain.model.CuteVoicemail

class VoicemailsRepository(
    private val context: Context
) {

    fun fetchVoicemails(): List<CuteVoicemail> {

        val voicemails = mutableListOf<CuteVoicemail>()

        val projection = arrayOf(
            VoicemailContract.Voicemails._ID,
            VoicemailContract.Voicemails.NUMBER,
            VoicemailContract.Voicemails.DURATION,
            VoicemailContract.Voicemails.DATE,
            // VoicemailContract.Voicemails.HAS_CONTENT, // Do we need that to assume voicemail has audio ?
        )

        context.contentResolver.query(
            VoicemailContract.Voicemails.CONTENT_URI,
            projection,
            null,
            null,
            "${VoicemailContract.Voicemails.DATE} DESC" // TODO: sort by most recent
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails._ID)
            val numberColumn = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails.NUMBER)
            val durationColumn = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails.DURATION)
            val dateColumn = cursor.getColumnIndexOrThrow(VoicemailContract.Voicemails.DATE)


            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val number = cursor.getString(numberColumn)
                val uri = ContentUris.withAppendedId(VoicemailContract.Voicemails.CONTENT_URI, id)
                val date = cursor.getLong(dateColumn)
                val duration = cursor.getLong(durationColumn)


                voicemails.add(
                    CuteVoicemail(
                        id = id,
                        address = number,
                        uri = uri,
                        duration = duration,
                        date = date
                    )
                )

            }
        }

        return voicemails

    }

}