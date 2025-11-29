package com.sosauce.cuteconnect.domain.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import com.sosauce.cuteconnect.domain.model.CuteContact

class ContactsRepository(
    private val context: Context
) {
    fun fetchContacts(): List<CuteContact> {


        val contactsMap = mutableMapOf<Long, CuteContact>()


        // This can be a bit confusing, but it's so much faster than having individual queries for each data, no more ANR and super fast loading ^_^
        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.Data.STARRED,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2,
            ContactsContract.Data.IS_PRIMARY,
        )

        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            null,
            null,
            "${ContactsContract.Data.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.CONTACT_ID)
            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val starredColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.STARRED)
            val data1Column = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)
            val mimeColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
            val data2Column = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA2)
            val isPrimaryColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.IS_PRIMARY)

            while (cursor.moveToNext()) {


                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)
                val isFavorite = cursor.getInt(starredColumn) != 0
                val mimeType = cursor.getString(mimeColumn)
                val data1 = cursor.getString(data1Column)
                val data2 = cursor.getInt(data2Column)
                val isPrimary = cursor.getInt(isPrimaryColumn) != 0


                val oldContact = contactsMap[id] ?: CuteContact(
                    id = id,
                    name = name,
                    photo = getContactPhoto(id),
                    phoneNumbers = emptyList(),
                    emails = emptyList(),
                    addresses = emptyList(),
                    websites = emptyList(),
                    notes = emptyList(),
                    events = emptyList(),
                    isFavorite = isFavorite,
                )

                val newContact = if (data1.isNullOrBlank()) {
                    oldContact
                } else {
                    when (mimeType) {
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                            oldContact.copy(phoneNumbers = oldContact.phoneNumbers + CuteContact.Phone(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                            oldContact.copy(emails = oldContact.emails + CuteContact.Email(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE ->
                            oldContact.copy(addresses = oldContact.addresses + CuteContact.Address(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE ->
                            oldContact.copy(websites = oldContact.websites + CuteContact.Website(data1))
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                            oldContact.copy(notes = oldContact.notes + CuteContact.Note(data1))
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE ->
                            oldContact.copy(events = oldContact.events + CuteContact.Event(data1, data2))
                        else -> oldContact
                    }
                }


                contactsMap[id] = newContact
            }
        }
        return contactsMap.values.toList()
    }

    // Since Coil takes care of loading the uri, we're fine passing the high res image
    private fun getContactPhoto(contactId: Long): Uri {
        val contactUri = ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId)
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
}