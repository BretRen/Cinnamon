@file:OptIn(DelicateCoroutinesApi::class)

package com.sosauce.cuteconnect.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.sosauce.cuteconnect.data.datastore.getArchivedConversations
import com.sosauce.cuteconnect.data.managers.MessageNotificationManager
import com.sosauce.cuteconnect.data.telephony.CuteTelephonyManager
import com.sosauce.cuteconnect.utils.getThreadIdOrCreate
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsReceiver : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != "android.provider.Telephony.SMS_DELIVER") return

        val messagesNotificationManager by inject<MessageNotificationManager>()
        val cuteTelephonyManager by inject<CuteTelephonyManager>()

        Sms.Intents.getMessagesFromIntent(intent).forEach { message ->

            val threadId = runBlocking(Dispatchers.IO) {
                message.displayOriginatingAddress?.getThreadIdOrCreate(context) ?: 0
            }

            cuteTelephonyManager.saveSmsToDevice(
                address = message.displayOriginatingAddress ?: "",
                message = message.messageBody,
                messageType = Sms.MESSAGE_TYPE_INBOX
            )

            val archived = runBlocking(Dispatchers.IO) { getArchivedConversations(context).first() }
            if (threadId.toString() in archived) return

            messagesNotificationManager.sendOrAppendMessageNotification(
                threadId = threadId,
                message = message.messageBody,
                number = message.displayOriginatingAddress
            )
        }
    }
}