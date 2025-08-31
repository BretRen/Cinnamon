package com.sosauce.cuteconnect.domain.repository

import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.CursorIndexOutOfBoundsException
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.CallLog
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.provider.MediaStore
import android.provider.Telephony
import android.provider.Telephony.Mms
import android.provider.Telephony.MmsSms
import android.provider.Telephony.Sms
import android.provider.VoicemailContract
import android.speech.tts.Voice
import android.telephony.CarrierConfigManager
import android.telephony.PhoneNumberUtils
import android.telephony.PhoneStateListener
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.util.SparseArray
import androidx.compose.ui.graphics.decodeToImageBitmap
import androidx.compose.ui.res.booleanResource
import androidx.compose.ui.util.fastForEach
import androidx.core.content.contentValuesOf
import androidx.core.net.toUri
import androidx.room.Room
import com.klinker.android.send_message.Message
import com.klinker.android.send_message.Settings
import com.klinker.android.send_message.Transaction
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingsDao
import com.sosauce.cuteconnect.data.conversation_settings.ConversationSettingsDatabase
import com.sosauce.cuteconnect.domain.model.CuteAttachment
import com.sosauce.cuteconnect.domain.model.CuteCallLog
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.domain.model.CuteConversation
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.domain.model.CuteSimCard
import com.sosauce.cuteconnect.domain.model.CuteVoicemail
import com.sosauce.cuteconnect.utils.PermissionUtils
import com.sosauce.cuteconnect.utils.copyMutate
import com.sosauce.cuteconnect.utils.observe
import com.sosauce.cuteconnect.utils.observeSims
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import java.io.File
import java.util.Locale

class CommonRepository(
    private val context: Context
) {

    private val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
    } else SmsManager.getDefault()

    private val subscriptionManager = context.getSystemService(SubscriptionManager::class.java)




    // CommonRepo is sometimes called from non DI'ed class' such as NotificationManagers, so just inject this here
//    private val conversationSettingsDao by lazy {
//        Room.databaseBuilder(
//            context = context,
//            klass = ConversationSettingsDatabase::class.java,
//            name = "conversationSettings.db"
//        ).allowMainThreadQueries().build().dao
//        // Should be safe to get draft snippet on main thread
//    }


    fun fetchLatestMessages(): Flow<List<CuteMessage>> {
        return context.contentResolver.observe(Sms.CONTENT_URI).map {
            fetchMessages()
        }
    }

    fun fetchLatestConversations(): Flow<List<CuteConversation>> {
        return context.contentResolver.observe(Telephony.Threads.CONTENT_URI).map {
            fetchConversations()
        }
    }

    fun fetchLatestContacts(): Flow<List<CuteContact>> {
        return context.contentResolver.observe(ContactsContract.Data.CONTENT_URI).map {
            fetchContacts()
        }
    }

    fun fetchLatestCallLog(): Flow<List<CuteCallLog>> {
        return context.contentResolver.observe(CallLog.Calls.CONTENT_URI).map {
            fetchCallLogs()
        }
    }

    fun fetchLatestVoicemails(): Flow<List<CuteVoicemail>> {
        return context.contentResolver.observe(VoicemailContract.Voicemails.CONTENT_URI).map {
            fetchVoicemails()
        }
    }

    fun fetchLatestSims(): Flow<List<CuteSimCard>> {
        return subscriptionManager.observeSims(context).map {
            fetchSims()
        }
    }




    private fun fetchMessages(): List<CuteMessage> {

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

        context.contentResolver.query(
            Sms.CONTENT_URI,
            projection,
            null,
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

            messages.addAll(fetchMms())
        }
        return messages
    }

    /* I think RCS messages from Google Messages are stored as MMS, same goes for group chat messages
    We also use this to retrieve attachments maybeee ?
    */
    private fun fetchMms(threadId: Long = -1): List<CuteMessage> {
        val mms = mutableListOf<CuteMessage>()

        val projection = arrayOf(
            Mms._ID,
            Mms.THREAD_ID,
            Mms.MESSAGE_BOX,
            Mms.READ,
            Mms.DATE,
//            Sms.BODY,
//            Sms.TYPE,
//            Sms.READ,
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

    private fun fetchConversations(
        onlyPinned: Boolean = false
    ): List<CuteConversation> {
        val conversations = mutableListOf<CuteConversation>()

        val projection = arrayOf(
            Telephony.Threads._ID,
            Telephony.Threads.SNIPPET,
            Telephony.Threads.DATE,
            Telephony.Threads.READ,
            Telephony.Threads.RECIPIENT_IDS
        )

        val selection = "${Telephony.Threads.MESSAGE_COUNT} > ?"

        context.contentResolver.query(
            "${Telephony.Threads.CONTENT_URI}?simple=true".toUri(),
            projection,
            selection,
            arrayOf("0"),
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

                conversations.add(
                    CuteConversation(
                        threadId = threadId,
                        snippet = snippet,
                        recipients = recipientsPhoneNumber,
                        contactsId = recipientsPhoneNumber.map { getContactIdForThread(it) },
                        isSenderBlocked = if (recipientsPhoneNumber.size > 1) false else BlockedNumbersManager.isNumberBlocked(recipientsPhoneNumber.first(), context), // TODO: Checked if anyone in the group chat is blocked
                        date = date,
                        read = read == 1,
                        isGroupChat = recipientsPhoneNumber.size > 1
                    )
                )
            }
        }
        return conversations
    }

    private fun fetchCallLogs(): List<CuteCallLog> {

        val callLogs = mutableListOf<CuteCallLog>()


        val projection = arrayOf(
            CallLog.Calls._ID,
            CallLog.Calls.NUMBER,
            CallLog.Calls.TYPE, // Incoming, outgoing, missed
            CallLog.Calls.DATE,
            CallLog.Calls.DURATION,
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
                        number = cursor.getString(numberColumn),
                        callType = cursor.getInt(callTypeColumn),
                        date = cursor.getLong(dateColumn),
                        duration = cursor.getLong(durationColumn)
                    )
                )
            }
        }

        return callLogs
    }
    // May or may not be the same feature Google Phone has
    private fun fetchVoicemails(): List<CuteVoicemail> {

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
            null // TODO: sort by most recent
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

    @SuppressLint("MissingPermission")
    fun fetchSims(): List<CuteSimCard> {
        val simCards = mutableListOf<CuteSimCard>()

        subscriptionManager.activeSubscriptionInfoList?.forEach { subInfo ->
            simCards.add(
                CuteSimCard(
                    subId = subInfo.subscriptionId,
                    name = subInfo.displayName?.toString() ?: "No name",
                    carrierName = subInfo.carrierName?.toString() ?: "No carrier",
                    color = subInfo.iconTint
                )
            )
        }
        return simCards
    }


    private fun fetchContacts(): List<CuteContact> {


        val contacts = mutableListOf<CuteContact>()
        val projection = arrayOf(
            ContactsContract.RawContacts.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.RawContacts.STARRED
        )
        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Data.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.CONTACT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val starredColumn = cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.STARRED)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val phoneNumbers = getContactPhoneNumbers(id)
                val isFavorite = cursor.getInt(starredColumn) != 0


                contacts.add(
                    CuteContact(
                        id = id,
                        name = name,
                        photo = getContactPhoto(id),
                        phoneNumbers = phoneNumbers,
                        emails = getContactEmails(id),
                        addresses = getContactAddresses(id),
                        websites = getContactWebsites(id),
                        isFavorite = isFavorite,
                    )
                )
            }
        }
        return contacts
    }

    private fun getListOfAddresses(ids: List<Long>): List<String> {
        val list = mutableListOf<String>()
        ids.fastForEach {
            list.add(getPhoneNumberOfRecipientId(it))
        }
        return list
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

    private fun getMmsThreadSnippet(threadId: Long): String {
        val latestMms = fetchMms(threadId).firstOrNull()
        val latestMessageId = latestMms?.id ?: 0
        var snippet = latestMms?.body ?: ""


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
        val selectionArgs = arrayOf(latestMessageId.toString())

        context.contentResolver.query(
            uri,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->

            while (cursor.moveToNext()) {
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

    // Since Coil takes care of loading the uri, we're fine passing the high res image
    private fun getContactPhoto(contactId: Long): Uri {
        val contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId.toLong())
        val photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO)

        val hasPhoto = try {
            context.contentResolver.openInputStream(photoUri)?.use {
                it.available() > 0
            } == true
        } catch (e: Exception) {
            false
        }
        return if (hasPhoto) {
            photoUri
        } else Uri.EMPTY
    }

    private fun getContactPhoneNumbers(contactId: Long): List<CuteContact.Phone> {
        val phoneNumbers = mutableListOf<CuteContact.Phone>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.NUMBER,
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.IS_PRIMARY
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                val isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY)) != 0

                phoneNumbers.add(
                    CuteContact.Phone(
                        number = number,
                        type = type,
                        isDefault = isDefault
                    )
                )
            }
        }

        return phoneNumbers
    }

    private fun getContactEmails(contactId: Long): List<CuteContact.Email> {
        val emails = mutableListOf<CuteContact.Email>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Email.ADDRESS,
            ContactsContract.CommonDataKinds.Email.TYPE,
            ContactsContract.CommonDataKinds.Email.IS_PRIMARY
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.Email.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val address = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS))
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.TYPE))
                val isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.IS_PRIMARY)) != 0

                emails.add(
                    CuteContact.Email(
                        email = address,
                        type = type,
                        isDefault = isDefault
                    )
                )
            }
        }

        return emails
    }

    private fun getContactAddresses(contactId: Long): List<CuteContact.Address> {
        val addresses = mutableListOf<CuteContact.Address>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
            ContactsContract.CommonDataKinds.StructuredPostal.IS_PRIMARY
        )

        context.contentResolver.query(
            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
            projection,
            "${ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val address = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS))
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.TYPE))
                val isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.StructuredPostal.IS_PRIMARY)) != 0

                addresses.add(
                    CuteContact.Address(
                        address = address,
                        type = type,
                        isDefault = isDefault
                    )
                )
            }
        }
        return addresses
    }

    private fun getContactWebsites(contactId: Long): List<CuteContact.Website> {

        val websites = mutableListOf<CuteContact.Website>()

        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Website.URL,
            ContactsContract.CommonDataKinds.Website.TYPE,
            ContactsContract.CommonDataKinds.Website.IS_PRIMARY
        )

        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            "${ContactsContract.RawContacts.CONTACT_ID} = ? AND ${ContactsContract.Data.MIMETYPE} = ?",
            arrayOf(contactId.toString(), "'${ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE}'"),
            null
        )?.use { cursor ->
            while (cursor.moveToNext()) {
                val website = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Website.URL)) ?: "test"
                val type = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Website.TYPE))
                val isDefault = cursor.getInt(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Website.IS_PRIMARY)) != 0

                websites.add(
                    CuteContact.Website(
                        website = website,
                        type = type,
                        isDefault = isDefault
                    )
                )
            }
        }

        return websites
    }


    // Inspired by https://github.com/FossifyOrg/Messages/blob/8c5bb9a32c990773259b4d95d698b83d31939171/app/src/main/kotlin/org/fossify/messages/extensions/Context.kt#L477
    private fun getMmsAttachment(messageId: Long): CuteAttachment {

        var attachment = CuteAttachment(messageId)

        val uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Mms.Part.CONTENT_URI
        } else {
            "content://mms/part".toUri()
        }

        val projection = arrayOf(
            Mms.Part._ID,
            Mms.Part.CONTENT_TYPE,
            Mms.Part.TEXT,
            Mms.Part.NAME,
        )
        val selection = "${Mms.Part.MSG_ID} = ?"
        val selectionArgs = arrayOf(messageId.toString())

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
                val filename = (cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.NAME)) ?: "").ifEmpty { "Unknown" }

                when (mimeType) {
                    "text/plain" -> {
                        val bodyText = cursor.getString(cursor.getColumnIndexOrThrow(Mms.Part.TEXT))
                        attachment = attachment.copy(
                            body = bodyText,
                        )
                    }
                    "application/smil" -> continue
                    else -> {
                        val fileUri = ContentUris.withAppendedId(uri, partId)

                        attachment = attachment.copy(
                            dataUri = attachment.dataUri.copyMutate { add(fileUri) },
                            filenames = attachment.filenames.copyMutate { add(filename) }
                        )
                    }
                }

            }

        }
        return attachment

    }

    fun saveSmsToDevice(
        cuteMessage: CuteMessage
    ) {

        val values = contentValuesOf(
            Sms.ADDRESS to cuteMessage.address,
            Sms.THREAD_ID to cuteMessage.threadId,
            Sms.DATE to System.currentTimeMillis(),
            Sms.BODY to cuteMessage.body,
            Sms.TYPE to cuteMessage.type,
            Sms.READ to cuteMessage.read
        )

        context.contentResolver.insert(Sms.CONTENT_URI, values)
    }

    fun deleteFromContentUri(
        contentUri: Uri,
        id: Long
    ) {
        val uri = ContentUris.withAppendedId(contentUri, id)
        context.contentResolver.delete(uri, null, null)
    }

    fun sendMessage(
        address: String,
        message: String
    ) {
        smsManager.sendTextMessage(address, null, message, null, null)

    }

    fun sendMms(
        cuteAttachment: CuteAttachment
    ) {

        val settings = Settings().apply {
            useSystemSending = true
        }
        val transaction = Transaction(context)
        val message = Message()
    }

    fun markSmsAsRead(
        messageId: Long
    ) {
        val contentValues = contentValuesOf(
            Sms.READ to 1
        )
        val selectionArgs = "${Sms._ID} = ?"

        context.contentResolver.update(Sms.CONTENT_URI, contentValues, selectionArgs, arrayOf(messageId.toString()))
    }


    val messages = fetchMessages()
    val conversations = fetchConversations()

}