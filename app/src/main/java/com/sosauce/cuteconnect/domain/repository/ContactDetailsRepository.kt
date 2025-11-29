package com.sosauce.cuteconnect.domain.repository

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.provider.ContactsContract.Contacts
import com.sosauce.cuteconnect.domain.model.CuteContact

class ContactDetailsRepository(
    private val context: Context
) {

    fun fetchContactDetails(contactId: Long): CuteContact {

        var contact = CuteContact()

        val projection = arrayOf(
            ContactsContract.Data.CONTACT_ID,
            ContactsContract.Data.DISPLAY_NAME,
            ContactsContract.Data.STARRED,
            ContactsContract.Data.MIMETYPE,
            ContactsContract.Data.DATA1,
            ContactsContract.Data.DATA2,
            ContactsContract.Data.IS_PRIMARY,
        )

        val selection = "${ContactsContract.Data.CONTACT_ID} = ?"
        val selectionArgs = arrayOf(contactId.toString())

        context.contentResolver.query(
            ContactsContract.Data.CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            null
        )?.use { cursor ->

            val nameColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.DISPLAY_NAME)
            val starredColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.STARRED)
            val data1Column = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA1)
            val mimeColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.MIMETYPE)
            val data2Column = cursor.getColumnIndexOrThrow(ContactsContract.Data.DATA2)
            val isPrimaryColumn = cursor.getColumnIndexOrThrow(ContactsContract.Data.IS_PRIMARY)

            while (cursor.moveToNext()) {
                val mimeType = cursor.getString(mimeColumn)
                val data1 = cursor.getString(data1Column)
                val data2 = cursor.getInt(data2Column)
                val isPrimary = cursor.getInt(isPrimaryColumn) != 0

                if (cursor.isFirst) {
                    contact = contact.copy(
                        name = cursor.getString(nameColumn),
                        isFavorite = cursor.getInt(starredColumn) != 0
                    )
                }


                if (!data1.isNullOrBlank()) {
                    contact = when (mimeType) {
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
            }
            return contact.copy(
                photo = getContactPhoto(contactId),
            )
        }
        return CuteContact(contactId)
    }

    // TODO have a separate more abstract class for everything image related
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