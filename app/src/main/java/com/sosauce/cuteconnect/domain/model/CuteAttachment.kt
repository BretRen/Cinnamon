package com.sosauce.cuteconnect.domain.model

import android.net.Uri

data class CuteAttachment(
    val id: Long = 0,
    val body: String = "",
    val attachmentDetails: List<AttachmentDetails> = emptyList()
) {
    data class AttachmentDetails(
        val id: Long,
        val uri: Uri,
        val filename: String
    )
}

