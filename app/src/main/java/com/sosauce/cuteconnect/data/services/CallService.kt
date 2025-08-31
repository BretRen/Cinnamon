package com.sosauce.cuteconnect.data.services

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.app.UiModeManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.telecom.Call
import android.telecom.CallAudioState
import android.telecom.CallEndpoint
import android.telecom.Connection
import android.telecom.ConnectionRequest
import android.telecom.ConnectionService
import android.telecom.InCallService
import android.telecom.PhoneAccountHandle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationManagerCompat
import com.sosauce.cuteconnect.activities.CallActivity
import com.sosauce.cuteconnect.data.managers.CallManager
import com.sosauce.cuteconnect.main.MainActivity
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import com.sosauce.cuteconnect.data.managers.CallNotificationManager
import com.sosauce.cuteconnect.data.receivers.CallReceiver
import com.sosauce.cuteconnect.domain.states.CallState
import com.sosauce.cuteconnect.domain.states.CompatCallEndpoint
import com.sosauce.cuteconnect.domain.states.CompatCallEndpoint.toCompat
import com.sosauce.cuteconnect.utils.ACCEPT_INCOMING_CALL
import com.sosauce.cuteconnect.utils.DECLINE_INCOMING_CALL
import com.sosauce.cuteconnect.utils.HANGUP_ONGOING_CALL
import com.sosauce.cuteconnect.utils.SWITCH_AUDIO_SOURCE
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.update

class CallService: InCallService() {


    private lateinit var audioManager: AudioManager
    val callNotificationManager by lazy { CallNotificationManager(this) }
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        var i = 0
        override fun run() {
            i++
            CallManager.updateTimeSpent(i)
            handler.postDelayed(this, 1000)
        }
    }

    private val callback = object : Call.Callback() {
        @SuppressLint("MissingPermission")
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)

            val notification = when(state) {
                Call.STATE_ACTIVE -> callNotificationManager.createOngoingBuilder(call)
                Call.STATE_RINGING, Call.STATE_DIALING -> callNotificationManager.createOutgoingBuilder(call)
                else -> callNotificationManager.createOngoingBuilder(call)
            }

            val callState = when(state) {
                Call.STATE_ACTIVE -> CallState.ONGOING
                Call.STATE_RINGING -> CallState.RINGING
                Call.STATE_DIALING -> CallState.DIALING
                Call.STATE_DISCONNECTED, Call.STATE_DISCONNECTING -> CallState.ENDED
                else -> CallState.ONGOING
            }
            CallManager.updateCallState(callState)
            CallManager.updateIsHolding(state == Call.STATE_HOLDING)

            callNotificationManager.sendNotification(notification)
        }

        override fun onDetailsChanged(call: Call?, details: Call.Details?) {
            super.onDetailsChanged(call, details)
            CallManager.updateNumber(details?.handle?.schemeSpecificPart ?: "Undetermined")
        }


    }

    private val audioFocus = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
        .setAudioAttributes(
            AudioAttributes
                .Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .build()
        )
        .build()

    override fun onCreate() {
        super.onCreate()
        audioManager = (getSystemService(AUDIO_SERVICE) as AudioManager).apply {
            requestAudioFocus(audioFocus)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioManager.abandonAudioFocusRequest(audioFocus)

    }



    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        CallManager.cuteCall = call
        CallManager.inCallService = this
        CallManager.cuteCall?.registerCallback(callback)
        handler.post(runnable)

        val isIncoming = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                call.details.state == Call.STATE_RINGING
            } else {
                call.state == Call.STATE_RINGING
            }

        if (isIncoming) {
            val isScreenLocked = (getSystemService(KEYGUARD_SERVICE) as KeyguardManager).isDeviceLocked

            val notification = callNotificationManager.createIncomingBuilder(call.details)
            callNotificationManager.sendNotification(notification)

            if (isScreenLocked) {
                val intent = Intent(this, CallActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                }
                startActivity(intent)
            }

        }
    }

    override fun onCallRemoved(call: Call?) {
        super.onCallRemoved(call)
        CallManager.cuteCall?.unregisterCallback(callback)
        CallManager.cuteCall = null
        CallManager.inCallService = null
        handler.removeCallbacks(runnable)
        callNotificationManager.clearCallNotifications()
    }

    override fun onAvailableCallEndpointsChanged(availableEndpoints: List<CallEndpoint?>) {
        super.onAvailableCallEndpointsChanged(availableEndpoints)

        CallManager.updateAvailableEndpoints(availableEndpoints)
    }

    override fun onCallEndpointChanged(callEndpoint: CallEndpoint) {
        super.onCallEndpointChanged(callEndpoint)

        // Yeah no we're not that's a A14 + thing, probably should be safe to just use audio state but I can't test it for now
        println("were on call endpoint changed Android 13")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            CallManager.updateCurrentEndpoint(
                endpoint = callEndpoint.toCompat()
            )
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCallAudioStateChanged(audioState: CallAudioState?) {
        super.onCallAudioStateChanged(audioState)
        CallManager.updateIsMuted(audioState?.isMuted == true)

        val endpoint = when(audioState?.route) {
            CallAudioState.ROUTE_EARPIECE -> CompatCallEndpoint.TYPE_EARPIECE
            CallAudioState.ROUTE_SPEAKER -> CompatCallEndpoint.TYPE_SPEAKER
            CallAudioState.ROUTE_BLUETOOTH -> CompatCallEndpoint.TYPE_BLUETOOTH
            CallAudioState.ROUTE_WIRED_HEADSET -> CompatCallEndpoint.TYPE_HEADSET
            else -> CompatCallEndpoint.TYPE_EARPIECE
        }
        CallManager.updateCurrentEndpoint(endpoint)
    }
}
