package com.sosauce.cinnamon.domain.states

import android.net.Uri

data class ConversationState(
    val photoUri: List<Uri> = emptyList()
)
