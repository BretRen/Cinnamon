package com.sosauce.cuteconnect.data.managers

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.telecom.Call
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.activities.CallActivity
import com.sosauce.cuteconnect.data.receivers.CallReceiver
import com.sosauce.cuteconnect.utils.ACCEPT_INCOMING_CALL
import com.sosauce.cuteconnect.utils.DECLINE_INCOMING_CALL
import com.sosauce.cuteconnect.utils.HANGUP_ONGOING_CALL
import com.sosauce.cuteconnect.utils.getContactNameOrNothing

class CallNotificationManager(
    private val context: Context,
) {

    val notificationManager = NotificationManagerCompat.from(context)
    val ringtone = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
    val incomingChannel = NotificationChannel(INCOMING_CHANNEL_ID, "Incoming calls", NotificationManager.IMPORTANCE_HIGH).apply {
        setSound(
            ringtone,
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
    }

    val intent = Intent(Intent.ACTION_MAIN, null).apply {
        flags = Intent.FLAG_ACTIVITY_NO_USER_ACTION
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        setClass(context, CallActivity::class.java)
    }

    val pendingIntent = PendingIntent.getActivity(context, 1, intent, PendingIntent.FLAG_MUTABLE)



    private val declineIntent = Intent(context, CallReceiver::class.java).apply {
        action = DECLINE_INCOMING_CALL
    }
    private val acceptIntent = Intent(context, CallReceiver::class.java).apply {
        action = ACCEPT_INCOMING_CALL
    }
    private val hangupIntent = Intent(context, CallReceiver::class.java).apply {
        action = HANGUP_ONGOING_CALL
    }

    private val declinePendingIntent = PendingIntent.getBroadcast(context, DECLINE_CALL_CODE, declineIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private val acceptPendingIntent = PendingIntent.getBroadcast(context, ACCEPT_CALL_CODE, acceptIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    private val hangupPendingIntent = PendingIntent.getBroadcast(context, HANGUP_ONGOING_CALL_CODE, hangupIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)


    @SuppressLint("MissingPermission")
    fun createIncomingNotification(
        callDetails: Call.Details
    ): Notification {

        val number = callDetails.gatewayInfo?.originalAddress?.schemeSpecificPart ?:
        callDetails.handle.schemeSpecificPart

        val builder = NotificationCompat.Builder(context, INCOMING_CHANNEL_ID)
            .setSmallIcon(R.drawable.round_call_received_24)
            .setCategory(Notification.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true)
            .setStyle(
                NotificationCompat.CallStyle.forIncomingCall(
                    Person.Builder()
                        .setName(number.getContactNameOrNothing(context))
                        .build(),
                    declinePendingIntent,
                    acceptPendingIntent

                )
            )
            .build()

        notificationManager.notify(CALL_NOTIF_ID, builder)

        return builder
    }

    @SuppressLint("MissingPermission")
    fun createOngoingNotification(
        callDetails: Call.Details
    ): Notification {

        val number = callDetails.gatewayInfo?.originalAddress?.schemeSpecificPart ?:
        callDetails.handle.schemeSpecificPart

        val builder = NotificationCompat.Builder(context, INCOMING_CHANNEL_ID)
            .setSmallIcon(R.drawable.call)
            .setCategory(Notification.CATEGORY_CALL)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setUsesChronometer(true)
            .setFullScreenIntent(pendingIntent, false)
            .setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    Person.Builder()
                        .setName(number.getContactNameOrNothing(context))
                        .build(),
                    hangupPendingIntent

                )
            )
            .build()

        notificationManager.notify(CALL_NOTIF_ID, builder)

        return builder
    }

    @SuppressLint("MissingPermission")
    fun createOutgoingNotification(
        callDetails: Call.Details
    ): Notification {

        val number = callDetails.gatewayInfo?.originalAddress?.schemeSpecificPart ?:
        callDetails.handle.schemeSpecificPart

        val builder = NotificationCompat.Builder(context, INCOMING_CHANNEL_ID)
            .setSmallIcon(R.drawable.round_call_made_24)
            .setCategory(Notification.CATEGORY_CALL)
            .setContentText(context.getString(R.string.ringing))
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setSilent(true)
            .setFullScreenIntent(pendingIntent, false)
            .setStyle(
                NotificationCompat.CallStyle.forOngoingCall(
                    Person.Builder()
                        .setName(number.getContactNameOrNothing(context))
                        .build(),
                    hangupPendingIntent

                )
            )
            .build()

        notificationManager.notify(CALL_NOTIF_ID, builder)

        return builder
    }

    init {
        notificationManager.createNotificationChannel(incomingChannel)
    }



    companion object {
        private const val DECLINE_CALL_CODE = 0
        private const val ACCEPT_CALL_CODE = 1
        private const val HANGUP_ONGOING_CALL_CODE = 2
        private const val INCOMING_CHANNEL_ID = "incoming calls"
        const val CALL_NOTIF_ID = 1
    }
}

