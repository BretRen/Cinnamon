package com.sosauce.cuteconnect.domain.states

import android.net.Uri

data class ConversationState(
    val photoUri: List<Uri> = emptyList()
)
