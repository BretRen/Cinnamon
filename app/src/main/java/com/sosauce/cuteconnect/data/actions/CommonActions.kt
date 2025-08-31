package com.sosauce.cuteconnect.data.actions

import android.net.Uri
import com.sosauce.cuteconnect.domain.model.CuteMessage

sealed interface CommonAction {

    data class SendMessage(
        val cuteMessage: CuteMessage
    ) : CommonAction
    data class MarkMessageAsRead(val messageId: Long) : CommonAction

    data class DeleteFromContentUri(
        val contentUri: Uri,
        val id: Long
    ) : CommonAction

}