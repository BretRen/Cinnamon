package com.sosauce.cuteconnect.domain.repository

import android.content.ContentProviderOperation
import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import android.widget.Toast
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastMap
import com.sosauce.cuteconnect.domain.model.CuteContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContactsRepository(
    private val context: Context
) {
    fun fetchContacts(): List<CuteContact> {


        val contactsMap = mutableMapOf<Long, CuteContact>()


        /* This can be a bit confusing, but it's so much faster than having individual queries for each data, no more ANR and superfast loading ^_^
            where data1 = the actual data (say a phone number) and data2 the type (home, work etc...)
        */
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.STARRED,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.RawContacts.ACCOUNT_NAME,
            ContactsContract.RawContacts.ACCOUNT_TYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2,
            ContactsContract.Data.IS_PRIMARY
        )

        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Data.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val displayNameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val starredColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.STARRED)
            val data1Column = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)
            val mimeColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
            val data2Column = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA2)
            val isPrimaryColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.IS_PRIMARY)
            val accountNameColumn = cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_NAME)
            val accountTypeColumn = cursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE)

            while (cursor.moveToNext()) {


                val id = cursor.getLong(idColumn)
                val mimeType = cursor.getString(mimeColumn)
                val data1 = cursor.getString(data1Column)
                val data2 = cursor.getInt(data2Column)
                val isPrimary = cursor.getInt(isPrimaryColumn) != 0


                // prevents remapping same data over and over for other fields, idk if it's actually useful but looks like it saves time idk!!
                if (!contactsMap.containsKey(id)) {
                    val isFavorite = cursor.getInt(starredColumn) != 0
                    val displayName = cursor.getString(displayNameColumn)
                    val accountName = cursor.getString(accountNameColumn) ?: "Device"
                    val accountType = cursor.getString(accountTypeColumn) ?: "device"

                    contactsMap[id] = CuteContact(
                        id = id,
                        displayName = displayName,
                        photo = getContactPhoto(id),
                        accountName = accountName,
                        accountType = accountType,
                        phoneNumbers = emptyList(),
                        emails = emptyList(),
                        addresses = emptyList(),
                        websites = emptyList(),
                        notes = emptyList(),
                        events = emptyList(),
                        isFavorite = isFavorite,
                    )
                }

                val contact = contactsMap[id] ?: continue

                val newContact = if (data1.isNullOrBlank()) {
                    continue
                } else {
                    when (mimeType) {
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                            contact.copy(phoneNumbers = contact.phoneNumbers + CuteContact.Phone(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                            contact.copy(emails = contact.emails + CuteContact.Email(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE ->
                            contact.copy(addresses = contact.addresses + CuteContact.Address(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE ->
                            contact.copy(websites = contact.websites + CuteContact.Website(data1))
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                            contact.copy(notes = contact.notes + CuteContact.Note(data1))
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE ->
                            contact.copy(events = contact.events + CuteContact.Event(data1, data2))
                        else -> contact
                    }
                }


                contactsMap[id] = newContact
            }
        }
        return contactsMap.values.toList()
    }

    private fun getContactPhoto(contactId: Long): Uri {
        val contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
        val photoUri = Uri.withAppendedPath(contactUri, Contacts.Photo.DISPLAY_PHOTO)
        return photoUri
    }

    suspend fun insertContact(
        cuteContact: CuteContact,
        showProgressToasts: Boolean = false // Only show them when inserting from vCard
    ) = withContext(Dispatchers.IO) {

        try {

            if (showProgressToasts) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Inserting contact", Toast.LENGTH_SHORT).show()
                }
            }
            val operations = arrayListOf<ContentProviderOperation>()
            val accountName = if (cuteContact.accountName == "Device") null else cuteContact.accountName
            val accountType = if (cuteContact.accountType == "device") null else cuteContact.accountType


            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, accountType)
                    .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, accountName)
                    .withYieldAllowed(true)
                    .build()
            )

            operations.add(
                ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, cuteContact.firstName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME, cuteContact.middleName)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, cuteContact.lastName)
                    .withYieldAllowed(true) // from what I understand, this allows the content resolver to not take too long/freeze thread for each operation
                    .build()
            )

            cuteContact.phoneNumbers.fastForEach { phone ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone.number)
                        .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, phone.type)
                        .withValue(ContactsContract.CommonDataKinds.Phone.IS_PRIMARY, if (phone.isDefault) 1 else 0)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            cuteContact.emails.fastForEach { email ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, email.email)
                        .withValue(ContactsContract.CommonDataKinds.Email.TYPE, email.type)
                        .withValue(ContactsContract.CommonDataKinds.Email.IS_PRIMARY, if (email.isDefault) 1 else 0)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            cuteContact.addresses.fastForEach { address ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, address.address)
                        .withValue(ContactsContract.CommonDataKinds.StructuredPostal.TYPE, address.type)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            cuteContact.websites.fastForEach { website ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Website.URL, website.website)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            cuteContact.notes.fastForEach { note ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Note.NOTE, note.note)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            cuteContact.events.fastForEach { event ->
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Event.START_DATE, event.date)
                        .withValue(ContactsContract.CommonDataKinds.Event.TYPE, event.type)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            if (cuteContact.nickname.isNotEmpty()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Nickname.NAME, cuteContact.nickname)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            if (cuteContact.organization.isNotEmpty() || cuteContact.jobPosition.isNotEmpty()) {
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                        .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, cuteContact.organization)
                        .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, cuteContact.jobPosition)
                        .withYieldAllowed(true)
                        .build()
                )
            }

            context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)

            if (showProgressToasts) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Contact inserted!", Toast.LENGTH_SHORT).show()
                }
            }
        } catch (_: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error while saving contact", Toast.LENGTH_SHORT).show()
            }
        }
    }

    suspend fun deleteContacts(contactIds: List<Long>) = withContext(Dispatchers.IO) {
        val uri = ContactsContract.RawContacts.CONTENT_URI
        val placeholder = contactIds.joinToString(", ") { "?" }
        val selection = "${ContactsContract.RawContacts.CONTACT_ID} IN ($placeholder)"
        val selectionArgs = contactIds.fastMap { it.toString() }.toTypedArray()

        context.contentResolver.delete(uri, selection, selectionArgs)
    }
}