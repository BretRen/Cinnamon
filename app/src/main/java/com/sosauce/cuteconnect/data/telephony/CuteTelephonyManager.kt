package com.sosauce.cuteconnect.data.telephony

import android.content.Context
import android.os.Build
import android.provider.Telephony.Sms
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import androidx.core.content.contentValuesOf
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.utils.getThreadIdOrCreate

class CuteTelephonyManager(
    private val context: Context
) {

    private val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        context.getSystemService(SmsManager::class.java)
    } else SmsManager.getDefault()


    fun markAsRead(messageId: Long) {
        val contentValues = contentValuesOf(
            Sms.READ to 1
        )
        val selection = "${Sms._ID} = ?"

        context.contentResolver.update(Sms.CONTENT_URI, contentValues, selection, arrayOf(messageId.toString()))
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