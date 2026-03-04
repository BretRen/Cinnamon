@file:OptIn(DelicateCoroutinesApi::class)

package com.sosauce.cuteconnect.data.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony.Sms
import com.sosauce.cuteconnect.data.datastore.UserPreferences
import com.sosauce.cuteconnect.data.managers.MessageNotificationManager
import com.sosauce.cuteconnect.data.telephony.CuteTelephonyManager
import com.sosauce.cuteconnect.utils.getThreadIdOrCreate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class SmsReceiver : BroadcastReceiver(), KoinComponent {
    override fun onReceive(context: Context, intent: Intent) {

        if (intent.action != "android.provider.Telephony.SMS_DELIVER") return

        val messagesNotificationManager by inject<MessageNotificationManager>()
        val cuteTelephonyManager by inject<CuteTelephonyManager>()
        val userPreferences by inject<UserPreferences>()

        Sms.Intents.getMessagesFromIntent(intent).forEach { message ->

            CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
                val threadId = message.displayOriginatingAddress?.getThreadIdOrCreate(context) ?: 0
                cuteTelephonyManager.saveSmsToDevice(
                    address = message.displayOriginatingAddress ?: "",
                    message = message.messageBody,
                    messageType = Sms.MESSAGE_TYPE_INBOX,
                    read = 0
                )
                val archived = userPreferences.archivedConversations.first()
                if (threadId.toString() in archived) return@launch
                messagesNotificationManager.sendOrAppendMessageNotification(
                    threadId = threadId,
                    message = message.messageBody,
                    number = message.displayOriginatingAddress
                )
            }
        }
    }
}