package com.sosauce.cuteconnect.domain.model

import android.net.Uri
import android.provider.Telephony
import kotlinx.serialization.Serializable

data class CuteAttachment(
    val id: Long = 0,
    val body: String = "",
    val dataUri: List<Uri> = emptyList(),
    val filenames: List<String> = emptyList()
)

