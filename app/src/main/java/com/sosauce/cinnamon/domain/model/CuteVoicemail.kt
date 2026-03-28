package com.sosauce.cinnamon.domain.model

import android.net.Uri

/**
 * @param duration This is in seconds
 */
data class CuteVoicemail(
    val id: Long,
    val address: String,
    val date: Long,
    val duration: Long,
    val uri: Uri,
)
