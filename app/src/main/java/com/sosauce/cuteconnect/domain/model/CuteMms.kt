package com.sosauce.cuteconnect.domain.model

data class CuteMms(
    val id: Long,
    val date: Long,
    val imagePath: String,
    val text: String?
)
