package com.sosauce.cinnamon.domain.repository

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.MmsSms
import androidx.compose.ui.util.fastForEach
import androidx.core.net.toUri
import com.sosauce.cinnamon.domain.model.CuteConversation

class ArchivedThreadsRepository(
    private val context: Context,
    private val blockedNumbersManager: BlockedNumbersManager
) {

    fun fetchArchivedThreads(
        threadIds: Set<String>
    ): List<CuteConversation> {
        val conversations = mutableListOf<CuteConversation>()

        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.DATE,
            Telephony.Threads.READ,
            Telephony.Threads.RECIPIENT_IDS
        )


        val selection = buildString {
            append("${Telephony.Threads.MESSAGE_COUNT} > ?")
            if (threadIds.isNotEmpty()) {
                append(" AND ")
                append(threadIds.joinToString(" OR ") { "${Telephony.Threads._ID} = ?" })
            }
        }

        val selectionArgs = buildList {
            add("0")
            if (threadIds.isNotEmpty()) {
                addAll(threadIds.sorted())
            }
        }.toTypedArray()


        println("hello - selection = $selection, selectionArgs = ${selectionArgs.contentToString()}")


        context.contentResolver.query(
            "${Telephony.Threads.CONTENT_URI}?simple=true".toUri(),
            projection,
            selection,
            selectionArgs,
            "${Telephony.Threads.DATE} DESC",
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Telephony.Threads._ID)
            val snippetColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.SNIPPET)
            val recipientIdsColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.RECIPIENT_IDS)
            val dateColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.DATE)
            val readColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.READ)


            while (cursor.moveToNext()) {

                val threadId = cursor.getLong(idColumn)
                val recipientIds = cursor.getString(recipientIdsColumn)
                val recipientIdsAsLongs = recipientIds.split(" ").map { it.toLongOrNull() ?: 0 }
                val recipientsPhoneNumber = getListOfAddresses(recipientIdsAsLongs)
                val snippet = (cursor.getString(snippetColumn) ?: "").ifEmpty { getMmsThreadSnippet(threadId) }
                val date = cursor.getLong(dateColumn)
                val read = cursor.getInt(readColumn)
                val isGroupChat = recipientsPhoneNumber.size > 1

                conversations.add(
                    CuteConversation(
                        threadId = threadId,
                        snippet = snippet,
                        recipients = recipientsPhoneNumber,
                        isSenderBlocked = if (recipientsPhoneNumber.size > 1) false else blockedNumbersManager.isNumberBlocked(recipientsPhoneNumber.first()), // TODO: Checked if anyone in the group chat is blocked
                        date = date,
                        read = read == 1,
                        isGroupChat = isGroupChat
                    )
                )
            }
        }
        println("hello - $conversations")
        return conversations
    }


    private fun getMmsThreadSnippet(threadId: Long): String {
        val latestMmsId = fetchLatestMmsId(threadId)
        var snippet = ""


        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Mms.Part.CONTENT_URI
        } else {
            "content://mms/part".toUri()
        }

        val projection = arrayOf(
            Mms._ID,
            Mms.Part.CONTENT_TYPE,
            Mms.Part.TEXT
        )
        val selection = "${Mms.Part.MSG_ID} = ?"
        val selectionArgs = arrayOf(latestMmsId.toString())

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->

            while (cursor.moveToFirst()) {
                val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.CONTENT_TYPE))

                when {
                    mimeType == "text/plain" -> {
                        val bodyText = cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.TEXT))
                        snippet = bodyText
                        break
                    }
                    mimeType.startsWith("image/") || mimeType.startsWith("video/") -> {
                        snippet = if (mimeType.startsWith("image/")) "Image" else "Video"
                        break
                    }
                    else -> {
                        snippet = "File"
                        break
                    }
                }

            }

        }
        return snippet
    }

    private fun fetchLatestMmsId(threadId: Long): Int {

        context.contentResolver.query(
            Mms.CONTENT_URI,
            arrayOf(Mms._ID),
            "${Mms.THREAD_ID} = ?",
            arrayOf(threadId.toString()),
            "${Mms.DATE} DESC LIMIT 1"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Mms._ID)

            while (cursor.moveToFirst()) {
                return cursor.getInt(idColumn)
            }
        }
        return 0
    }


    private fun getListOfAddresses(ids: List<Long>): List<String> {
        val list = mutableListOf<String>()
        ids.fastForEach {
            list.add(getPhoneNumberOfRecipientId(it))
        }
        return list
    }

    // Personal note: MMS also represent group chats in the SMS (probably not RCS) system
    private fun getPhoneNumberOfRecipientId(id: Long): String {
        val uri = Uri.withAppendedPath(MmsSms.CONTENT_URI, "canonical-addresses")
        val projection = arrayOf(
            Mms.Addr.ADDRESS
        )

        val selection = "${Mms._ID} = ?"
        val selectionArgs = arrayOf(id.toString())
        context.contentResolver.query(uri, projection, selection, selectionArgs, null)?.use { cursor ->
            val addressColumn = cursor.getColumnIndexOrThrow(Mms.Addr.ADDRESS)
            if (cursor.moveToFirst()) {
                return cursor.getString(addressColumn)
            }
        }
        return ""
    }

    /**
     * Retrieves a contact's ID from it's address, or -1 if it doesn't exist. This is later used to get a contact by ID
     */
    private fun getContactIdForThread(address: String): Long {

        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
            address
        )

        context.contentResolver.query(
            uri,
            arrayOf(ContactsContract.PhoneLookup._ID),
            null,
            null,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.PhoneLookup._ID)
            if (cursor.moveToFirst()) {
                return cursor.getLong(idColumn)
            }
        }

        return -1
    }

}