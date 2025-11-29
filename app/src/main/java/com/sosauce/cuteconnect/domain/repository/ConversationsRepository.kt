@file:OptIn(ExperimentalCoroutinesApi::class)

package com.sosauce.cuteconnect.domain.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.MmsSms
import android.provider.Telephony.Sms
import android.util.Xml
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import androidx.core.net.toUri
import com.sosauce.cuteconnect.domain.model.CuteAttachment
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.utils.PermissionUtils
import com.sosauce.cuteconnect.utils.copyMutate
import com.sosauce.cuteconnect.utils.observe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import org.xmlpull.v1.XmlPullParser

class ConversationsRepository(
    private val context: Context
) {
    fun fetchLatestMessagesForThread(threadId: Long): Flow<List<CuteMessage>> {
        return context.contentResolver.observe(Sms.CONTENT_URI).mapLatest {
            fetchMessagesForThread(threadId)
        }.flowOn(Dispatchers.IO)
    }


    private fun fetchMessagesForThread(threadId: Long): List<CuteMessage> {

        if (!PermissionUtils.hasSmsPermission(context)) return emptyList()



        val messages = mutableListOf<CuteMessage>()

        val projection = arrayOf(
            Sms._ID,
            Sms.THREAD_ID,
            Sms.ADDRESS,
            Sms.DATE,
            Sms.BODY,
            Sms.TYPE,
            Sms.READ,
        )

        val selection = "${Sms.THREAD_ID} = ?"
        val selectionArgs = arrayOf(threadId.toString())

        context.contentResolver.query(
            Sms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null,
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Sms._ID)
            val threadIdColumn = cursor.getColumnIndexOrThrow(Sms.THREAD_ID)
            val addressColumn = cursor.getColumnIndexOrThrow(Sms.ADDRESS)
            val dateColumn = cursor.getColumnIndexOrThrow(Sms.DATE)
            val bodyColumn = cursor.getColumnIndexOrThrow(Sms.BODY)
            val typeColumn = cursor.getColumnIndexOrThrow(Sms.TYPE)
            val readColumn = cursor.getColumnIndexOrThrow(Sms.READ)


            while (cursor.moveToNext()) {
                messages.add(
                    CuteMessage(
                        id = cursor.getLong(idColumn),
                        body = cursor.getString(bodyColumn),
                        type = cursor.getInt(typeColumn),
                        threadId = cursor.getLong(threadIdColumn),
                        address = cursor.getString(addressColumn),
                        date = cursor.getLong(dateColumn),
                        read = cursor.getInt(readColumn) == 1
                    )
                )
            }

            messages.addAll(fetchThreadMms(threadId))
        }
        return messages.sortedBy { it.date }
    }

    private fun fetchThreadMms(threadId: Long): List<CuteMessage> {
        val mms = mutableListOf<CuteMessage>()

        val projection = arrayOf(
            Mms._ID,
            Mms.THREAD_ID,
            Mms.MESSAGE_BOX,
            Mms.READ,
            Mms.DATE
        )

        val selection = "${Mms.THREAD_ID} = ?"

        val selectionArgs = arrayOf(threadId.toString())


        context.contentResolver.query(
            Mms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Mms._ID)
            val threadIdColumn = cursor.getColumnIndexOrThrow(Mms.THREAD_ID)
            val typeColumn = cursor.getColumnIndexOrThrow(Mms.MESSAGE_BOX)
            val dateColumn = cursor.getColumnIndexOrThrow(Mms.DATE)
            val readColumn = cursor.getColumnIndexOrThrow(Mms.READ)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val threadId = cursor.getLong(threadIdColumn)
                val type = cursor.getInt(typeColumn)
                val rawDate = cursor.getLong(dateColumn)
                val date = if (rawDate > 1_000L) rawDate * 1000 else System.currentTimeMillis()
                val attachment = getMmsAttachment(id)
                val read = cursor.getInt(readColumn) == 1

                mms.add(
                    CuteMessage(
                        id = id,
                        body = attachment.body,
                        type = type,
                        threadId = threadId,
                        address = "", // TODO() get address
                        date = date,
                        read = read,
                        attachment = attachment,
                        isMms = true
                    )
                )
            }
        }
        return mms
    }

    fun fetchThreadRecipients(threadId: Long): List<String> {
        val recipients = mutableListOf<String>()

        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.RECIPIENT_IDS
        )
        context.contentResolver.query(
            "${Telephony.Threads.CONTENT_URI}?simple=true".toUri(),
            projection,
            "${Telephony.Threads._ID} = ?",
            arrayOf(threadId.toString()),
            null,
        )?.use { cursor ->
            val recipientIdsColumn = cursor.getColumnIndexOrThrow(Telephony.Threads.RECIPIENT_IDS)

            while (cursor.moveToNext()) {

                val recipientIds = cursor.getString(recipientIdsColumn)
                val recipientIdsAsLongs = recipientIds.split(" ").map { it.toLongOrNull() ?: 0 }
                recipients.addAll(getListOfAddresses(recipientIdsAsLongs))
            }
        }
        return recipients
    }

    private fun getListOfAddresses(ids: List<Long>): List<String> {
        val list = mutableListOf<String>()
        ids.fastForEach {
            list.add(getPhoneNumberOfRecipientId(it))
        }
        return list
    }
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


    private fun fetchMms(threadId: Long = -1): List<CuteMessage> {
        val mms = mutableListOf<CuteMessage>()

        val projection = arrayOf(
            Mms._ID,
            Mms.THREAD_ID,
            Mms.MESSAGE_BOX,
            Mms.READ,
            Mms.DATE
        )

        val selection = if (threadId != -1L) {
            "${Mms.THREAD_ID} = ?"
        } else null

        val selectionArgs = if (threadId != -1L) {
            arrayOf(threadId.toString())
        } else null

        val sortOrder = if (threadId != -1L) {
            "${Mms.DATE} DESC LIMIT 1"
        } else null

        context.contentResolver.query(
            Mms.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(Mms._ID)
            val threadIdColumn = cursor.getColumnIndexOrThrow(Mms.THREAD_ID)
            val typeColumn = cursor.getColumnIndexOrThrow(Mms.MESSAGE_BOX)
            val dateColumn = cursor.getColumnIndexOrThrow(Mms.DATE)
            val readColumn = cursor.getColumnIndexOrThrow(Mms.READ)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val threadId = cursor.getLong(threadIdColumn)
                val type = cursor.getInt(typeColumn)
                val rawDate = cursor.getLong(dateColumn)
                val date = if (rawDate > 1_000L) rawDate * 1000 else System.currentTimeMillis()
                val attachment = getMmsAttachment(id)
                val read = cursor.getInt(readColumn) == 1

                mms.add(
                    CuteMessage(
                        id = id,
                        body = attachment.body,
                        type = type,
                        threadId = threadId,
                        address = "", // TODO() get address
                        date = date,
                        read = read,
                        attachment = attachment,
                        isMms = true
                    )
                )
            }
        }
        return mms
    }

    // Inspired by https://github.com/FossifyOrg/Messages/blob/8c5bb9a32c990773259b4d95d698b83d31939171/app/src/main/kotlin/org/fossify/messages/extensions/Context.kt#L477
    private fun getMmsAttachment(
        messageId: Long,
    ): CuteAttachment {
        var attachment = CuteAttachment(messageId)

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Mms.Part.CONTENT_URI
        } else {
            "content://mms/part".toUri()
        }

        val projection = arrayOf(
            Mms.Part._ID,
            Mms.Part.CONTENT_TYPE,
            Mms.Part.TEXT
        )
        val selection = "${Mms.Part.MSG_ID} = ?"
        val selectionArgs = arrayOf(messageId.toString())

        var attachmentNames: List<String>? = null
        var attachmentCount = 0

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->

            while (cursor.moveToNext()) {
                val partId = cursor.getLong(cursor.getColumnIndexOrThrow(Mms._ID)) // Id meant to get the data uri of MMS if any
                val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.CONTENT_TYPE))

                if (mimeType == "text/plain") {
                    val bodyText = cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.TEXT))
                    attachment = attachment.copy(
                        body = bodyText
                    )
                } else if (mimeType.startsWith("image/") || mimeType.startsWith("video/")) {
                    val fileUri = Uri.withAppendedPath(uri, partId.toString())
                    val attachmentDetail = CuteAttachment.AttachmentDetails(
                        id = partId,
                        uri = fileUri,
                        filename = ""
                    )

                    attachment = attachment.copy(
                        attachmentDetails = attachment.attachmentDetails.copyMutate { add(attachmentDetail) },
                    )
                } else if (mimeType != "application/smil") {
                    val fileUri = Uri.withAppendedPath(uri, partId.toString())
                    val attachmentName = attachmentNames?.getOrNull(attachmentCount) ?: ""

                    val attachmentDetail = CuteAttachment.AttachmentDetails(
                        id = partId,
                        uri = fileUri,
                        filename = attachmentName
                    )

                    attachment = attachment.copy(
                        attachmentDetails = attachment.attachmentDetails.copyMutate { add(attachmentDetail) }
                    )
                    attachmentCount++
                } else {
                    val text = cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.TEXT))
                    attachmentNames = parseAttachmentNames(text)

                }
            }
        }
        return attachment
    }

    private fun parseAttachmentNames(text: String): List<String> {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(text.reader())
        parser.nextTag()
        return readSmil(parser)
    }

    private fun readSmil(parser: XmlPullParser): List<String> {
        parser.require(XmlPullParser.START_TAG, null, "smil")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "body") {
                return readBody(parser)
            } else {
                skip(parser)
            }
        }

        return emptyList()
    }

    private fun readBody(parser: XmlPullParser): List<String> {
        val names = mutableListOf<String>()
        parser.require(XmlPullParser.START_TAG, null, "body")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }

            if (parser.name == "par") {
                parser.require(XmlPullParser.START_TAG, null, "par")
                while (parser.next() != XmlPullParser.END_TAG) {
                    if (parser.eventType != XmlPullParser.START_TAG) {
                        continue
                    }

                    if (parser.name in listOf("img", "audio", "video", "vcard", "ref")) {
                        names.add(parser.getAttributeValue(null, "src"))
                        skip(parser)
                    } else {
                        skip(parser)
                    }
                }
            } else {
                skip(parser)
            }
        }
        return names
    }

    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }

        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }

    fun deleteConversation(threadId: Long) {
        context.contentResolver.delete(Sms.CONTENT_URI, "${Sms.THREAD_ID} = ?", arrayOf(threadId.toString()))
        context.contentResolver.delete(Mms.CONTENT_URI, "${Mms.THREAD_ID} = ?", arrayOf(threadId.toString()))
    }

}