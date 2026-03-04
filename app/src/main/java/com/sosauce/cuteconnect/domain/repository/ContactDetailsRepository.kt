package com.sosauce.cuteconnect.domain.repository

import android.content.Context
import android.provider.ContactsContract
import com.sosauce.cuteconnect.domain.model.CuteContact
import com.sosauce.cuteconnect.utils.beautifyNumber
import com.sosauce.cuteconnect.utils.getContactPfpUriFromId

class ContactDetailsRepository(
    private val context: Context
) {
    fun fetchContactDetails(contactId: Long): CuteContact {


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

            var name = ""
            var isFavorite = false
            val phoneNumbers = mutableListOf<CuteContact.Phone>()
            val emails = mutableListOf<CuteContact.Email>()
            val addresses = mutableListOf<CuteContact.Address>()
            val websites = mutableListOf<CuteContact.Website>()
            val notes = mutableListOf<CuteContact.Note>()
            val events = mutableListOf<CuteContact.Event>()

            while (cursor.moveToNext()) {
                if (cursor.isFirst) {
                    name = cursor.getString(nameColumn)
                    isFavorite = cursor.getInt(starredColumn) != 0
                }
                val mimeType = cursor.getString(mimeColumn)
                val data1 = cursor.getString(data1Column)
                val data2 = cursor.getInt(data2Column)
                val isPrimary = cursor.getInt(isPrimaryColumn) != 0

                if (!data1.isNullOrBlank()) {
                    when (mimeType) {
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE ->
                            phoneNumbers.add(CuteContact.Phone(data1.beautifyNumber(), data2, isPrimary))
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE ->
                            emails.add(CuteContact.Email(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE ->
                            addresses.add(CuteContact.Address(data1, data2, isPrimary))
                        ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE ->
                            websites.add(CuteContact.Website(data1))
                        ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE ->
                            notes.add(CuteContact.Note(data1))
                        ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE ->
                            events.add(CuteContact.Event(data1, data2))
                    }
                }
            }

            return CuteContact(
                id = contactId,
                firstName = name,
                photo = contactId.getContactPfpUriFromId(),
                isFavorite = isFavorite,
                phoneNumbers = phoneNumbers,
                emails = emails,
                addresses = addresses,
                websites = websites,
                notes = notes,
                events = events
            )
        }



        return CuteContact(contactId)
    }
}