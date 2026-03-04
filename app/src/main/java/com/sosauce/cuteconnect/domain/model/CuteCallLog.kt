package com.sosauce.cuteconnect.domain.model

data class CuteCallLog(
    val id: Long,
    val rawNumber: String,
    val beautifiedNumberOrName: String,
    val callType: Int,
    val date: Long,
    val duration: Long,
    val country: String?
)