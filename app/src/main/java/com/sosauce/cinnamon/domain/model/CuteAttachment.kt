package com.sosauce.cinnamon.domain.model

import android.net.Uri
import com.sosauce.cinnamon.domain.UriSerializer
import kotlinx.serialization.SerialInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

data class CuteAttachment(
    val id: Long = 0,
    val body: String = "",
    val attachmentDetails: List<AttachmentDetails> = emptyList()
) {
    data class AttachmentDetails(
        val id: Long,
        val uri: Uri,
        val filename: String,
        val attachmentType: AttachmentType,
        val size: Long
    )
}


enum class AttachmentType {
    IMAGE,
    VIDEO,
    AUDIO,
    VCARD,
    OTHER
}

