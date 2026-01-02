package com.sosauce.cuteconnect.data.telephony

import android.content.Context
import android.os.Build
import android.provider.Telephony.Sms
import android.telephony.SmsManager
import androidx.core.content.contentValuesOf
import com.sosauce.cuteconnect.utils.getThreadIdOrCreate

class CuteTelephonyManager(
    private val context: Context
) {

    private val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
    } else SmsManager.getDefault()


    fun markConversationAsRead(threadId: Long) {
        val contentValues = contentValuesOf(
            Sms.READ to 1
        )
        val selection = "${Sms.THREAD_ID} = ?"

        context.contentResolver.update(Sms.CONTENT_URI, contentValues, selection, arrayOf(threadId.toString()))
    }

    fun sendSms(
        address: String,
        message: String
    ) {
        smsManager.sendTextMessage(address, null, message, null, null)
        saveSmsToDevice(address, message)
    }

    fun saveSmsToDevice(
        address: String,
        message: String,
        messageType: Int = Sms.MESSAGE_TYPE_SENT
    ) {

        val values = contentValuesOf(
            Sms.ADDRESS to address,
            Sms.THREAD_ID to address.getThreadIdOrCreate(context),
            Sms.DATE to System.currentTimeMillis(),
            Sms.BODY to message,
            Sms.TYPE to messageType,
            Sms.READ to 1
        )

        context.contentResolver.insert(Sms.CONTENT_URI, values)
    }

}