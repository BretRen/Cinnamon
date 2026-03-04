package com.sosauce.cuteconnect.domain.model

import android.net.Uri

data class CuteContact(
    val id: Long = 0,
    val displayName: String = "",
    val firstName: String = "",
    val middleName: String = "",
    val lastName: String = "",
    val organization: String = "",
    val nickname: String = "",
    val jobPosition: String = "",
    val accountName: String = "",
    val accountType: String = "",
    val photo: Uri = Uri.EMPTY,
    val isFavorite: Boolean = false,
    val phoneNumbers: List<Phone> = emptyList(),
    val emails: List<Email> = emptyList(),
    val addresses: List<Address> = emptyList(),
    val websites: List<Website> = emptyList(),
    val notes: List<Note> = emptyList(),
    val events: List<Event> = emptyList()
) {


    data class Email(
        val email: String,
        val type: Int,
        val isDefault: Boolean
    )

    data class Phone(
        val number: String,
        val type: Int,
        val isDefault: Boolean
    )

    data class Address(
        val address: String,
        val type: Int,
        val isDefault: Boolean
    )

    data class Website(
        val website: String
    )

    data class Note(
        val note: String
    )

    data class Event(
        val date: String,
        val type: Int
    )

}
