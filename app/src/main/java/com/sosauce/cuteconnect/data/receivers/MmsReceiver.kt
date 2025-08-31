package com.sosauce.cuteconnect.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.sosauce.cuteconnect.data.managers.MessageNotificationManager
import com.sosauce.cuteconnect.domain.repository.CommonRepository

class MmsReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {

        val messagesNotificationManager by lazy { MessageNotificationManager(context) }
        val commonRepository by lazy { CommonRepository(context) }

        val pdu = intent?.getByteArrayExtra("data") ?: return
    }
}