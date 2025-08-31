package com.sosauce.cuteconnect.domain.model

data class CuteCallLog(
    val id: Long,
    val number: String,
    val callType: Int,
    val date: Long,
    val duration: Long
)