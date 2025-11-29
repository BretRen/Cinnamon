package com.sosauce.cuteconnect.data.managers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Telephony
import androidx.core.app.NotificationCompat
import androidx.core.app.Person
import androidx.core.app.RemoteInput
import com.sosauce.cuteconnect.R
import com.sosauce.cuteconnect.data.receivers.MessageReplyReceiver
import com.sosauce.cuteconnect.data.receivers.SmsReceiver
import com.sosauce.cuteconnect.data.telephony.CuteTelephonyManager
import com.sosauce.cuteconnect.domain.model.CuteMessage
import com.sosauce.cuteconnect.main.MainActivity
import com.sosauce.cuteconnect.utils.CuteIntents
import com.sosauce.cuteconnect.utils.RESULT_KEY
import com.sosauce.cuteconnect.utils.THREAD_ID
import com.sosauce.cuteconnect.utils.getAddressFromThreadId
import com.sosauce.cuteconnect.utils.getContactNameOrNothing
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class MessageNotificationManager(
    private val context: Context,
    private val cuteTelephonyManager: CuteTelephonyManager
) {

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val flag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        PendingIntent.FLAG_MUTABLE
    } else 0

    // TODO: make this navigate to the discussion
    private val intent = Intent(context, MainActivity::class.java)
    private val contentIntent = PendingIntent.getActivity(context, 1001, intent, PendingIntent.FLAG_IMMUTABLE)



    /**
     * This should only be used the reply to a message via a notification
     * @param message String message given by the remote input, NOT a CuteMessage
     */
    fun reply(
        threadId: Long,
        message: String,
        person: Person
    ) {
        // Nullable tho if we're replying to a notification obviously it should exist
        val threadIdAsNotif = notificationManager.activeNotifications.find { it.id == threadId.toInt() }
        val messageStyle = NotificationCompat.MessagingStyle.Message(message, System.currentTimeMillis(), person)

        threadIdAsNotif?.let { notification ->

            val activeStyle = NotificationCompat.MessagingStyle
                .extractMessagingStyleFromNotification(notification.notification)
                ?.addMessage(messageStyle)
            notificationManager.notify(
                threadId.toInt(),
                NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
                    .setContentIntent(contentIntent)
                    .setChannelId(MESSAGES_CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_icon)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setStyle(activeStyle)
                    .addAction(replyAction(threadId))
                    .build()
            )
            cuteTelephonyManager.sendSms(
                address = threadId.getAddressFromThreadId(context),
                message = message
            )
        }
    }



    fun sendOrAppendMessageNotification(
        threadId: Long,
        message: String,
        number: String?,
        imageUri: Uri? = null
    ) {

        val person = Person.Builder()
            .setName(number?.getContactNameOrNothing(context) ?: "No one")
            .build()
        val receivedMessage = NotificationCompat.MessagingStyle.Message(message, System.currentTimeMillis(), person).apply {
            if (imageUri != null) {
                setData("image/", imageUri)
            }
        }
        val notificationStyle = NotificationCompat.MessagingStyle(person)
            .addMessage(receivedMessage)

        val threadIdAsNotif = notificationManager.activeNotifications.find { it.id == threadId.toInt() }

        val intent = Intent(context, MainActivity::class.java).apply {
            action = CuteIntents.NOTIFICATION_NAVIGATE_TO_THREAD
            putExtra("threadId", threadId)
        }
        val pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT )

        threadIdAsNotif?.let { notification ->
            val activeStyle = NotificationCompat.MessagingStyle.extractMessagingStyleFromNotification(notification.notification)?.addMessage(receivedMessage)

            notificationManager.notify(
                threadId.toInt(),
                NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
                    .setContentIntent(contentIntent)
                    .setChannelId(MESSAGES_CHANNEL_ID)
                    .setSmallIcon(R.drawable.app_icon)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                    .setStyle(activeStyle)
                    .addAction(replyAction(threadId))
                    .setContentIntent(pendingIntent)
                    .build()
            )
        } ?: notificationManager.notify(
            threadId.toInt(),
            NotificationCompat.Builder(context, MESSAGES_CHANNEL_ID)
                .setContentIntent(contentIntent)
                .setChannelId(MESSAGES_CHANNEL_ID)
                .setSmallIcon(R.drawable.app_icon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setStyle(notificationStyle)
                .addAction(replyAction(threadId))
                .setContentIntent(pendingIntent)
                .build()
        )
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = context.getString(R.string.messages)
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(MESSAGES_CHANNEL_ID, name, importance)
        notificationManager.createNotificationChannel(channel)
    }

    private fun replyAction(
        threadId: Long
    ): NotificationCompat.Action {
        val replyIntent = Intent(context, MessageReplyReceiver::class.java).apply {
            putExtra(THREAD_ID, threadId)
        }
        val replyPendingIntent = PendingIntent.getBroadcast(context, 1, replyIntent, flag)

        val remoteInput = RemoteInput.Builder(RESULT_KEY)
            .setLabel(context.getString(R.string.type_here))
            .build()

        return NotificationCompat.Action.Builder(
            0,
            context.getString(R.string.reply),
            replyPendingIntent
        ).addRemoteInput(remoteInput).build()
    }

    companion object {
        private const val MESSAGES_CHANNEL_ID = "Incoming Message"
        private const val MESSAGES_CHANNEL_ID_INT = 100
    }
}