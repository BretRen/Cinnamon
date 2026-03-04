package com.sosauce.cuteconnect.domain.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.MmsSms
import androidx.compose.ui.util.fastFilter
import androidx.compose.ui.util.fastMap
import androidx.core.net.toUri
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.utils.beautifyNumber
import com.sosauce.cuteconnect.utils.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class MessagesRepository(
    private val context: Context,
    private val blockedNumbersManager: BlockedNumbersManager
) {


    fun fetchLatestConversations(): Flow<List<CuteConversation>> {
        return context.contentResolver.observe(Telephony.Threads.CONTENT_URI).map {
            fetchConversations()
        }.flowOn(Dispatchers.IO)
    }

    private fun fetchConversations(): List<CuteConversation> {
        data class Row(
            val threadId: Long,
            val recipientIds: List<Long>,
            val snippet: String?,
            val date: Long,
            val read: Boolean,
        )

        val rows = mutableListOf<Row>()

        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.DATE,
            Telephony.Threads.READ,
            Telephony.Threads.RECIPIENT_IDS
        )


        context.contentResolver.query(
            "${Telephony.Threads.CONTENT_URI}?simple=true".toUri(),
            projection,
            "${Telephony.Threads.MESSAGE_COUNT} > ?",
            arrayOf("0"),
            "${Telephony.Threads.DATE} DESC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Telephony.Threads._ID)
            val snippetColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.SNIPPET)
            val recipientIdColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.RECIPIENT_IDS)
            val dateColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.DATE)
            val readColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.READ)

            while (cursor.moveToNext()) {
                val recipientIds = cursor.getString(recipientIdColumn)
                    .split(" ")
                    .fastMap { it.toLongOrNull() ?: 0 }

                rows.add(
                    Row(
                        threadId = cursor.getLong(idColumn),
                        recipientIds = recipientIds,
                        snippet = cursor.getString(snippetColumn),
                        date = cursor.getLong(dateColumn),
                        read = cursor.getInt(readColumn) == 1,
                    )
                )
            }
        }

        if (rows.isEmpty()) return emptyList()

        val allRecipientIds = rows.flatMapTo(mutableListOf()) { it.recipientIds }
        val phoneById = fetchPhoneNumbersByRecipientIds(allRecipientIds)
        val nameByPhoneNumber = fetchContactNames(phoneById.values.toList())

        val threadsNeedingSnippet = rows.fastFilter { it.snippet == null }.fastMap { it.threadId }
        val mmsSnippetToThreadId = threadsNeedingSnippet.associateWith { getMmsThreadSnippet(it) }

        return rows.fastMap { row ->
            val rawRecipients = row.recipientIds.fastMap { phoneById[it] ?: "" }
            val recipients = rawRecipients.fastMap { phone ->
                nameByPhoneNumber[phone]?.beautifyNumber() ?: phone.beautifyNumber()
            }
            val isGroupChat = rawRecipients.size > 1

            CuteConversation(
                threadId = row.threadId,
                snippet = row.snippet ?: mmsSnippetToThreadId[row.threadId] ?: "",
                rawRecipients = rawRecipients,
                recipients = recipients,
                isSenderBlocked = if (isGroupChat) false else blockedNumbersManager.isNumberBlocked(rawRecipients.first()),
                date = row.date,
                read = row.read,
                isGroupChat = isGroupChat,
            )
        }
    }

    private fun fetchPhoneNumbersByRecipientIds(ids: List<Long>): Map<Long, String> {
        if (ids.isEmpty()) return emptyMap()
        val result = mutableMapOf<Long, String>()

        val uri = Uri.withAppendedPath(MmsSms.CONTENT_URI, "canonical-addresses")


        context.contentResolver.query(
            uri,
            null,
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Mms.Addr._ID)
            val addrColumn = cursor.getColumnIndexOrThrow(Mms.Addr.ADDRESS)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                if (id in ids) result[id] = cursor.getString(addrColumn) ?: ""
            }
        }
        return result
    }

    private fun fetchContactNames(phoneNumbers: List<String>): Map<String, String> {
        if (phoneNumbers.isEmpty()) return emptyMap()
        val result = mutableMapOf<String, String>()
        val placeholders = phoneNumbers.joinToString(",") { "?" }
        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ),
            "${ContactsContract.CommonDataKinds.Phone.NUMBER} IN ($placeholders)",
            phoneNumbers.toTypedArray(),
            null,
        )?.use { cursor ->
            val numberColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val number = cursor.getString(numberColumn)
                val name = cursor.getString(nameColumn)

                result[number] = name
            }
        }
        return result
    }

private fun getMmsThreadSnippet(threadId: Long): String {
    val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        Mms.Part.CONTENT_URI
    } else {
        "content://mms/part".toUri()
    }

    val projection = arrayOf(
        Mms.Part.CONTENT_TYPE,
        Mms.Part.TEXT,
    )
    // today i learnt u can cross SQL select
    val selection = "${Mms.Part.MSG_ID} = (SELECT ${Mms._ID} FROM $MMS_TABLE_NAME WHERE ${Mms.THREAD_ID} = ? ORDER BY ${Mms.DATE} DESC LIMIT 1)"
    val selectionArgs = arrayOf(threadId.toString())

    context.contentResolver.query(
        uri,
        projection,
        selection,
        selectionArgs,
        null,
    )?.use { cursor ->
        var fallback = ""
        while (cursor.moveToNext()) {
            val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.CONTENT_TYPE))
            fallback = when {
                mimeType == "text/plain" -> {
                    return cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.TEXT)) ?: ""
                }
                mimeType.startsWith("image/") -> context.getString(R.string.image)
                mimeType.startsWith("video/") -> context.getString(R.string.video)
                else -> context.getString(R.string.attachment)
            }
        }
        return fallback
    }

    return ""
}

    suspend fun deleteConversation(threadId: Long) = withContext(Dispatchers.IO) {
        context.contentResolver.delete(Telephony.Sms.CONTENT_URI, "${Telephony.Sms.THREAD_ID} = ?", arrayOf(threadId.toString()))
        context.contentResolver.delete(Mms.CONTENT_URI, "${Mms.THREAD_ID} = ?", arrayOf(threadId.toString()))
        context.contentResolver.delete(Telephony.Threads.CONTENT_URI, "${Telephony.Threads._ID} = ?", arrayOf(threadId.toString()))
    }

    companion object {
        // extracted by logging an MMS selection query, hope it's correct
        const val MMS_TABLE_NAME = "pdu"
    }

}